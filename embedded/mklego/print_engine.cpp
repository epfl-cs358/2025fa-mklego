#include "print_engine.h"
#include "ui.h"
#include "physics.h"
#include "melodies.h"
#include "lgcode.h"
#include "dispenser.h"

int printState = 0; // 0 = config, 1 = printing
// -----------------------------
// PRINT FUNCTIONS (EMPTY SHELLS)
// Run LGCODE / Print logic
void runLGCodeFromSD(String filename) {
  lcd.clear();
  // Dim the backlight gradually for printing mood
  for (int b = 255; b >= 120; b -= 5) {
      analogWrite(lcdBacklight, b);
      delay(20);
  }

  lcd.print("Printing:");
  lcd.setCursor(0,1);
  lcd.print(filename);
  delay(300);

   // preheat animation (visual only)
  preheatAnimation();

  killTriggered = false;
  printStartMillis = millis();

  File f = SD.open(filename, FILE_READ);

  if (!f) {
    lcd.clear();
    lcd.print("Open error!");
    delay(1000);
    return;
  }

  // For ETA calculation
  unsigned long startTime = millis();

  while (f.available() && !killTriggered) {
    // progress display
    int progress = map(f.position(), 0, f.size(), 0, 100);

    unsigned long elapsed = millis() - startTime;
    unsigned long eta = 0;
    if (progress > 2) {
      eta = (elapsed * 100UL / progress) - elapsed;
    }
    int etaSec = eta / 1000;

    // Row 0 → Print ..%
    lcd.setCursor(0, 0);
    lcd.print("Print ");
    lcd.print(progress);
    lcd.print("%   ");   // spaces clear old chars

    // Row 2 → "ETA: ..s"
    lcd.setCursor(0, 2);
    lcd.print("ETA:");
    lcd.print(etaSec);
    lcd.print("s   ");

    // Row 3 → graphical progress bar
    drawProgressBar(progress);

    uint8_t b = f.read();
    write_lgcode(&b, 1);

    appState = 99;   // printing state
    long x;
    long y;
    long z = 0;
    long dispX;
    long r;
    if (printState == 0 && has_current_operation()){
      appState = 98; // dispenser placement state
      printState = 1;
      startDispenserMenu();
      return;
    }
    
    // Process completed operations
    while (has_current_operation() && !killTriggered) {

      switch (current_operation_type()) {

        case MOVE: {
          x = get_move_operation().x;
          y = get_move_operation().y;
          z = get_move_operation().z;

          dispensorMoveReferential().moveTo(0, 0, max(z, 2));     // safe retract
          plateMoveReferential().moveTo(x, y, max(z, 2));         // move above spot
          plateWiggleReferential().moveTo(x, y, z);               // descend
          plateWiggleReferential().wiggle(x, y, z);               // top wiggle
          plateDownReferential().moveTo(x, y, z);                 // final set
          plateDownReferential().wiggle(x, y, z);                 // bottom wiggle
        } break;

        case ROTATE: {
          r = get_rotate_operation().rotation;
          rotateNozzle(r);
        } break;

        case GRAB: {
          const brick_type* brick = get_type(get_grab_operation().brick_id);
          //long dispX = get_grab_operation().attachment_id;

          const dispenser* disp = get_dispenser(brick);
          if (disp) {
            dispX = disp->pos + 1; //to test with different positions
          } else {
            // No dispenser found for this brick, use default position
            dispX = get_grab_operation().attachment_id;
            if (brick->size_x == 2) {
              dispX += 5;
            }
          }
/*           if (brick->size_x == 2) {
              dispX += 5;
          } */

          dispensorMoveReferential().moveTo(dispX, 0, max(z, 2));
          nozzleUp();
          dispensorDownReferential().moveTo(dispX, 0, 0);
          dispensorMoveReferential().moveTo(dispX, 0, max(z, 2));
        } break;

        case DROP: {
          nozzleDown();
          plateMoveReferential().moveTo(x, y, max(z, 2));
        } break;
      }

      pop_current_operation();
    }
  }

  f.close();

  lcd.clear();
  if (killTriggered) {
    lcd.print("Stopped!");
    applyLCDTheme(2);   // flash
    delay(300);
    applyLCDTheme(1);
  } else {
    unsigned long totalSec = (millis() - printStartMillis) / 1000;
    lcd.print("Print complete!");
    delay(300);
    lcd.clear();
    //playMelody(marioNotes, marioDurations, marioLength);
    lcd.print("Print complete");
    lcd.setCursor(0,1); lcd.print("Time: ");
    lcd.print(totalSec);
    lcd.print("s");
    delay(2200);
    // Flash effect
    analogWrite(lcdBacklight, 255);
    delay(80);
    analogWrite(lcdBacklight, 80);
    delay(80);
    analogWrite(lcdBacklight, 255);
    delay(80);

    // Restore bright
    for (int b = 120; b <= 255; b += 5) {
        analogWrite(lcdBacklight, b);
        delay(15);
    }
  }

  delay(800);
  lastA = digitalRead(encA);
  appState = 0;
  showMainMenu();
}


void playSongFromSD(String filename){
  File song = SD.open(filename);
  if (!song) {
    lcd.clear(); lcd.print("Error opening song!");
    delay(1000); return;
  }

  lcd.clear(); lcd.print("Playing:"); lcd.setCursor(0,1); lcd.print(filename);
  delay(300);

  // Reset kill flag
  killTriggered = false;

  while (song.available() && !killTriggered) {
    String note = song.readStringUntil('\n');
    note.trim();
    if (!song.available()) break;
    String durStr = song.readStringUntil('\n');
    durStr.trim();
    if (note.length() == 0 || durStr.length() == 0) continue;

    int duration = durStr.toInt();
    int freq = getNoteFrequency(note);
    int noteDuration = 1000 / max(1, duration);

    // immediate check
    if (killTriggered) break;

    if (freq > 0) buzz(buzzerPin, freq, noteDuration);
    else delay(noteDuration);

    if (killTriggered) break;

    delay(noteDuration * 1); // small gap
  }

  song.close();

  if (killTriggered) {
    lcd.clear(); lcd.print("Stopped!"); delay(500);
    killTriggered = false;
    appState = 0;
    showMainMenu();
    return;
  }

  lcd.clear(); lcd.print("Done playing!"); delay(800);
}

// map textual note to frequency
int getNoteFrequency(String note) {
  note.trim(); note.toUpperCase();
  if (note == "REST" || note == "0") return 0;
  if (note == "C4") return NOTE_C4;
  if (note == "D4") return NOTE_D4;
  if (note == "E4") return NOTE_E4;
  if (note == "F4") return NOTE_F4;
  if (note == "G4") return NOTE_G4;
  if (note == "A4") return NOTE_A4;
  if (note == "B4") return NOTE_B4;
  if (note == "C5") return NOTE_C5;
  if (note == "D5") return NOTE_D5;
  if (note == "E5") return NOTE_E5;
  if (note == "F5") return NOTE_F5;
  if (note == "G5") return NOTE_G5;
  if (note == "A5") return NOTE_A5;
  if (note == "B5") return NOTE_B5;
  if (note == "C6") return NOTE_C6;
  if (note == "D6") return NOTE_D6;
  if (note == "E6") return NOTE_E6;
  if (note == "G6") return NOTE_G6;
  if (note == "A6") return NOTE_A6;
  if (note == "B6") return NOTE_B6;
  if (note == "C7") return NOTE_C7;
  if (note == "D7") return NOTE_D7;
  if (note == "E7") return NOTE_E7;
  return 0;
}

// generate square wave for length ms at frequency Hz
void buzz(int targetPin, long frequency, long length){
  if (frequency <= 0) { delay(length); return; }
  long delayValue = 1000000L / frequency / 2L;
  long numCycles = frequency * length / 1000L;
  for (long i = 0; i < numCycles; i++) {
    digitalWrite(targetPin, HIGH);
    delayMicroseconds(delayValue);
    digitalWrite(targetPin, LOW);
    delayMicroseconds(delayValue);
    if (killTriggered) break; // allow early exit
  }
}

void playMelody(const uint8_t *notes, const uint8_t *durations, int len) {
  for (int i = 0; i < len; i++) {
    uint8_t note = pgm_read_byte(&notes[i]);
    uint8_t dur  = pgm_read_byte(&durations[i]);

    int duration = 1000 / dur;

    if (note > 0) buzz(buzzerPin, note, duration);
    else delay(duration);

    delay(duration * 0.30);
  }
}

void playFinishBeep() {
  buzz(buzzerPin, 1200, 120);
  delay(80);
  buzz(buzzerPin, 1500, 120);
  delay(80);
  buzz(buzzerPin, 2000, 200);
}