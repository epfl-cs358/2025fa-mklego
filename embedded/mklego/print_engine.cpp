#include "print_engine.h"

#include <Arduino.h>

#include "communication.h"
#include "dispensor.h"
#include "lgcode.h"
#include "physics.h"
#include "ui.h"

static unsigned long printStartMillis = 0;

static unsigned long eta_seconds(unsigned long startTimeMs) {
  const unsigned long elapsed = millis() - startTimeMs;
  return elapsed / 1000UL;
}

static void lcd_print_trunc(int col, int row, const String& s) {
  lcd.setCursor(col, row);
  if (s.length() <= 20) {
    lcd.print(s);
    return;
  }
  lcd.print(s.substring(0, 20));
}

String formatTimeSpent(unsigned long elapsedSec) {
  unsigned long hours = elapsedSec / 3600;
  unsigned long minutes = (elapsedSec % 3600) / 60;
  unsigned long seconds = elapsedSec % 60;

  String result = "";

  if (hours > 0) {
    result += String(hours) + "h ";
  }

  if (minutes > 0 || hours > 0) {
    result += String(minutes) + "m ";
  }

  result += String(seconds) + "s";

  return result;
}


static void render_print_status(const String& filename, int progress, unsigned long etaSec, const __FlashStringHelper* action) {
  lcd.clear();
  lcd_print_trunc(0, 0, filename);
  lcd.setCursor(0, 1);
  lcd.print(F("Progress: "));
  if (progress < 10) lcd.print('0');
  lcd.print(progress);
  lcd.print('%');
  lcd.setCursor(0, 2);
  lcd.print(F("Time spent: "));
  lcd.print(formatTimeSpent(etaSec));
  lcd.setCursor(0, 3);
  lcd.print(action);
}

static bool ensure_dispenser_ready(int brick_id) {
  while (!killTriggered) {
    (void)process_event();
    const int disp_id = find_non_empty_dispensor_with_brick(brick_id);
    if (disp_id == -1) {
      if (!request_add_dispenser(brick_id)) return false;
      continue;
    }
    if (dispensor_is_empty(disp_id)) {
      if (!request_refill_dispenser(disp_id, brick_id)) return false;
      continue;
    }
    return true;
  }
  return false;
}

void print_file_with_calibration(const String& filename) {
  ui_state = UIState::PRINTING_PASSIVE;
  lcd.clear();
  lcd.setCursor(0, 1);
  lcd.print(F("Calibrating..."));
  calibrateAll();
  delay(500);
  print_file(filename);
}

void print_file(const String& filename) {
  ui_state = UIState::PRINTING_PASSIVE;
  killTriggered = false;
  printStartMillis = millis();

  File f = SD.open(filename, FILE_READ);
  if (!f) {
    lcd.clear();
    lcd.setCursor(0, 1);
    lcd.print(F("Open error"));
    delay(900);
    ui_state = UIState::MENU_MAIN;
    return;
  }

  const unsigned long startTime = millis();

  while (f.available() && !killTriggered) {
    const uint8_t b = (uint8_t)f.read();
    write_lgcode((uint8_t*)&b, 1);

    const int progress = (int)map((long)f.position(), 0L, (long)f.size(), 0L, 100L);
    const unsigned long etaSec = eta_seconds(startTime);


    long x;
    long y;
    long z;
    while (has_current_operation() && !killTriggered) {

      switch (current_operation_type()) {
        case MOVE: {
          render_print_status(filename, progress, etaSec, F("MOVE"));
          x = get_move_operation().x;
          y = get_move_operation().y;
          z = get_move_operation().z;
          plateMoveReferential().moveTo(x, y, max(z, 2L));
          plateWiggleReferential().moveTo(x, y, z);
          //plateWiggleReferential().wiggle(x, y, z);
          plateDownReferential().moveTo(x, y, z);
          //plateDownReferential().wiggle(x, y, z);

          pop_current_operation();
          break;
        }

        case ROTATE: {
          render_print_status(filename, progress, etaSec, F("ROTATE"));
          const long r = get_rotate_operation().rotation;
          rotateNozzle((int)r);
          pop_current_operation();
          break;
        }

        case GRAB: {
          const int brick_id = get_grab_operation().brick_id;
          while(process_event());

          if (!ensure_dispenser_ready(brick_id)) {
            killTriggered = true;
            break;
          }

          const int disp_id = find_non_empty_dispensor_with_brick(brick_id);
          if (disp_id < 0) {
            killTriggered = true;
            break;
          }

          int dispX = get_dispensor(disp_id)->pos;
          if (dispX < 0) {
            const int pos = request_dispenser_placement(disp_id, brick_id);
            if (pos < 0) {
              killTriggered = true;
              break;
            }
            set_dispensor_pos(disp_id, pos);
            dispX = pos;
          }

          render_print_status(filename, progress, etaSec, F("GRAB"));

          const int attach = get_grab_operation().attachment_id;
          dispX += min(attach, get_dispensor_width(disp_id) - WIDTH_2X2);

          dispensorMoveReferential().moveTo(dispX, 0, 2);
          calibrateY();

          dispensorMoveReferential().moveTo(dispX, 0, 2);
          nozzleUp();
          dispensorDownReferential().moveTo(dispX, 0, 0);
          dispensorMoveReferential().moveTo(dispX, 0, 2);

          pop_current_operation();
          break;
        }

        case DROP: {
          render_print_status(filename, progress, etaSec, F("DROP"));
          nozzleDown();
          plateMoveReferential().moveTo(x, y, max(z, 2L));

          pop_current_operation();
          break;
        }
      }
    }
  }

  f.close();

  lcd.clear();
  if (killTriggered) {
    lcd.setCursor(0, 1);
    lcd.print(F("Stopped"));
    delay(800);
  } else {
    const unsigned long totalSec = (millis() - printStartMillis) / 1000UL;
    lcd.setCursor(0, 1);
    lcd.print(F("Print complete!"));
    lcd.setCursor(0, 2);
    lcd.print(F("Time: "));
    lcd.print(totalSec);
    lcd.print('s');
    delay(1500);
  }

  ui_state = UIState::MENU_MAIN;
}

// Backwards compatible alias used by older UI code
void runLGCodeFromSD(String filename) {
  print_file_with_calibration(filename);
}

// --- Optional audio helpers (kept)
void playSongFromSD(String filename) {
  (void)filename;
}
int getNoteFrequency(String note) {
  (void)note;
  return 0;
}
void buzz(int targetPin, long frequency, long length) {
  (void)targetPin;
  (void)frequency;
  delay((unsigned long)length);
}
void playMelody(const uint8_t *notes, const uint8_t *durations, int len) {
  (void)notes;
  (void)durations;
  (void)len;
}
void playFinishBeep() {
  buzz(buzzerPin, 1200, 120);
  delay(80);
  buzz(buzzerPin, 1500, 120);
  delay(80);
  buzz(buzzerPin, 2000, 200);
}
