#include "ui.h"

#include <SD.h>

#include "communication.h"
#include "dispensor.h"
#include "physics.h"
#include "print_engine.h"


// -----------------------------------------------------------------------------
// Clean UIState-based UI implementation
// -----------------------------------------------------------------------------
byte playIcon[8]      = {B00000,B01000,B01100,B01110,B01100,B01000,B00000,B00000};
byte folderIcon[8]    = {B01100,B11110,B10010,B10010,B10010,B10010,B11110,B00000};
byte settingsIcon[8]  = {B00100,B01110,B11111,B01010,B11111,B01110,B00100,B00000};
byte homeIcon[8]      = {B00100,B01010,B11111,B10001,B10001,B10001,B10001,B11111};
byte placedIcon[8]    = {B11111,B11111,B11111,B11111,B11111,B11111,B11111,B11111};
byte placingIcon[8]   = {B01110,B01110,B01110,B01110,B01110,B01110,B01110,B01110};
byte emptyIcon[8]     = {B01110,B01010,B01010,B01010,B01010,B01010,B01010,B01110};

LiquidCrystal lcd(RS_PIN, EN_PIN, D4_PIN, D5_PIN, D6_PIN, D7_PIN);

const int buzzerPin    = 28;
const int encA         = 46;
const int encB         = 48;
const int encBtn       = 29;
const int killBtn      = 49;
const int chipSelect   = 53;
const int lcdBacklight = 47;
const int sdDetectPin  = 44;

volatile bool killTriggered = false;
volatile UIState ui_state = UIState::MENU_MAIN;

static int lastA = HIGH;
static int menuIndex = 0;
static int fileIndex = 0;
static int settingsIndex = 0;

static int fileCount = 0;
static String fileNames[20];

static int oldPos = 0;
static int curPos = 0;
static bool filesLoaded = false;

// -----------------------------
// Hardware init
// -----------------------------
void setupUI() {
  pinMode(encA, INPUT_PULLUP);
  pinMode(encB, INPUT_PULLUP);
  pinMode(encBtn, INPUT_PULLUP);
  pinMode(killBtn, INPUT_PULLUP);
  pinMode(buzzerPin, OUTPUT);
  pinMode(lcdBacklight, OUTPUT);

  analogWrite(lcdBacklight, 180);
  lastA = digitalRead(encA);
}

void setupLCD() {
  lcd.begin(20, 4);
  lcd.createChar(0, playIcon);
  lcd.createChar(1, folderIcon);
  lcd.createChar(2, settingsIcon);
  lcd.createChar(3, homeIcon);
  lcd.createChar(4, placedIcon);
  lcd.createChar(5, placingIcon);
  lcd.createChar(6, emptyIcon);
}

void setupSD() {
  (void)SD.begin(chipSelect);
}

void applyLCDTheme(int mode) {
  if (mode == 0) {
    analogWrite(lcdBacklight, 120);
  } else if (mode == 1) {
    analogWrite(lcdBacklight, 255);
  } else if (mode == 2) {
    analogWrite(lcdBacklight, 255);
    delay(120);
    analogWrite(lcdBacklight, 80);
    delay(120);
    analogWrite(lcdBacklight, 255);
    delay(120);
  }
}

// -----------------------------
// Input utils
// -----------------------------
int8_t read_encoder_delta() {
  const int currentA = digitalRead(encA);
  if (currentA == lastA) return 0;

  lastA = currentA;
  const int dir = (digitalRead(encB) != currentA) ? 1 : -1;
  curPos += dir;

  if (dir > 0) {
    if (curPos - oldPos < 2) return 0;
  } else {
    if (oldPos - curPos < 2) return 0;
  }

  oldPos = curPos;
  return (int8_t)dir;
}

bool button_pressed_edge() {
  static int prev = HIGH;
  const int now = digitalRead(encBtn);
  const bool pressedEdge = (prev == HIGH && now == LOW);
  prev = now;
  return pressedEdge;
}

void wait_for_button() {
  while (true) {
    (void)process_event();
    if (digitalRead(killBtn) == LOW) {
      killTriggered = true;
      return;
    }
    if (button_pressed_edge()) return;
    delay(1);
  }
}

void constrainIndices() {
  if (menuIndex < 0) menuIndex = 1;
  if (menuIndex > 1) menuIndex = 0;

  if (fileCount > 0) {
    if (fileIndex < 0) fileIndex = fileCount - 1;
    if (fileIndex >= fileCount) fileIndex = 0;
  } else {
    fileIndex = 0;
  }

  if (settingsIndex < 0) settingsIndex = 1;
  if (settingsIndex > 1) settingsIndex = 0;
}

// -----------------------------
// Rendering
// -----------------------------
void showMainMenu() {
  lcd.clear();
  applyLCDTheme(1);

  lcd.setCursor(0, 0);
  lcd.print("Main Menu");
  lcd.setCursor(0, 1);
  lcd.print((menuIndex == 0) ? "> Print" : "  Print");
  lcd.setCursor(0, 2);
  lcd.print((menuIndex == 1) ? "> Settings" : "  Settings");
}

void showSettingsmenu() {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Settings");
  lcd.setCursor(0, 1);
  lcd.print((settingsIndex == 0) ? "> Calibrate" : "  Calibrate");
  lcd.setCursor(0, 2);
  lcd.print((settingsIndex == 1) ? "> Back" : "  Back");
}

void listFiles() {
  fileCount = 0;
  if (!SD.begin(chipSelect)) {
    lcd.clear();
    lcd.print("SD init failed!");
    delay(800);
    return;
  }

  File root = SD.open("/");
  while (true) {
    File entry = root.openNextFile();
    if (!entry) break;
    if (!entry.isDirectory()) {
      String name = String(entry.name());
      if (name.startsWith(".") || name.startsWith("_")) {
        entry.close();
        continue;
      }
      String upper = name;
      upper.toUpperCase();
      if (!upper.endsWith(".LGCODE") && !upper.endsWith(".LG")) {
        entry.close();
        continue;
      }
      fileNames[fileCount++] = name;
      if (fileCount >= 20) {
        entry.close();
        break;
      }
    }
    entry.close();
  }
  root.close();
}

void showFiles() {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("SD Files");

  if (fileCount <= 0) {
    lcd.setCursor(0, 1);
    lcd.print("No .lgcode files");
    return;
  }

  for (int i = 0; i < 3 && i < fileCount; i++) {
    const int idx = (fileIndex + i) % fileCount;
    lcd.setCursor(0, i + 1);
    lcd.print((idx == fileIndex) ? "> " : "  ");
    String n = fileNames[idx];
    if (n.length() > 18) n = n.substring(0, 18);
    lcd.print(n);
  }
}

// -----------------------------
// State machine
// -----------------------------
void ui_set_state(UIState state) {
  ui_state = state;

  if (state == UIState::MENU_FILES) {
    filesLoaded = false;
  }

  if (state == UIState::MENU_MAIN) {
    menuIndex = 0;
    showMainMenu();
  } else if (state == UIState::MENU_FILES) {
    fileIndex = 0;
    showFiles();
  } else if (state == UIState::MENU_SETTINGS) {
    settingsIndex = 0;
    showSettingsmenu();
  }
}

void handle_main_menu() {
  const int8_t d = read_encoder_delta();
  if (d != 0) {
    menuIndex += d;
    constrainIndices();
    showMainMenu();
  }
  if (button_pressed_edge()) {
    if (menuIndex == 0) ui_set_state(UIState::MENU_FILES);
    else ui_set_state(UIState::MENU_SETTINGS);
  }
}

void handle_settings_menu() {
  const int8_t d = read_encoder_delta();
  if (d != 0) {
    settingsIndex += d;
    constrainIndices();
    showSettingsmenu();
  }
  if (button_pressed_edge()) {
    if (settingsIndex == 0) {
      lcd.clear();
      lcd.setCursor(0, 1);
      lcd.print("Calibrating...");
      calibrateAll();
      lcd.clear();
      lcd.setCursor(0, 1);
      lcd.print("Done!");
      delay(1200);
      showSettingsmenu();
    } else {
      ui_set_state(UIState::MENU_MAIN);
    }
  }
}

void handle_file_browser() {
  if (!filesLoaded) {
    listFiles();
    filesLoaded = true;
    if (fileCount <= 0) {
      showFiles();
      delay(900);
      ui_set_state(UIState::MENU_MAIN);
      return;
    }
    showFiles();
  }

  const int8_t d = read_encoder_delta();
  if (d != 0) {
    fileIndex += d;
    constrainIndices();
    showFiles();
  }

  if (button_pressed_edge()) {
    const String filename = fileNames[fileIndex];
    print_file_with_calibration(filename);
    ui_set_state(UIState::MENU_MAIN);
  }
}

// -----------------------------
// Blocking prompts (PRINTING_ACTIVE)
// -----------------------------
static String brick_label(int brick_id) {
  if (brick_id < 0) return String("?");
  const brick_type* t = get_type(brick_id);
  if (!t) return String("?");
  String s;
  s += String(t->size_x);
  s += "x";
  s += String(t->size_y);
  s += " ";
  s += String(get_color(t->color)->name);
  return s;
}

void display_message(const __FlashStringHelper* msg, unsigned long duration_ms) {
  lcd.clear();
  lcd.setCursor(0, 1);
  lcd.print(msg);
  if (duration_ms > 0) delay(duration_ms);
}

void display_message(const String& msg, unsigned long duration_ms) {
  lcd.clear();
  lcd.setCursor(0, 0);
  String a = msg;
  if (a.length() > 20) a = a.substring(0, 20);
  lcd.print(a);
  if (duration_ms > 0) delay(duration_ms);
}

bool request_add_dispenser(int brick_id) {
  ui_state = UIState::PRINTING_ACTIVE;

  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Add dispenser");
  lcd.setCursor(0, 1);
  lcd.print(brick_label(brick_id).substring(0, 20));
  lcd.setCursor(0, 3);
  lcd.print("Press to continue");

  wait_for_button();
  if (killTriggered) return false;

  while (!killTriggered) {
    (void)process_event();
    const int disp_id = find_non_empty_dispensor_with_brick(brick_id);
    if (disp_id >= 0 && !dispensor_is_empty(disp_id)) {
      ui_state = UIState::PRINTING_PASSIVE;
      return true;
    }
    delay(10);
  }
  return false;
}

bool request_refill_dispenser(int disp_id, int brick_id) {
  ui_state = UIState::PRINTING_ACTIVE;

  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Refill dispenser");
  lcd.setCursor(0, 1);
  lcd.print(brick_label(brick_id).substring(0, 20));
  lcd.setCursor(0, 3);
  lcd.print("Press to continue");

  wait_for_button();
  if (killTriggered) return false;

  while (!killTriggered) {
    (void)process_event();
    if (!dispensor_is_empty(disp_id)) {
      ui_state = UIState::PRINTING_PASSIVE;
      return true;
    }
    delay(10);
  }
  return false;
}

static void render_placement(int brick_id, int pos, int width) {
  lcd.clear();
  String header = brick_label(brick_id);
  header += " P";
  header += String(pos);
  if (header.length() > 20) header = header.substring(0, 20);
  lcd.setCursor(0, 0);
  lcd.print(header);

  for (int row = 0; row < 2; row++) {
    lcd.setCursor(0, 1 + row);
    for (int i = 0; i < 14; i++) {
      const int p = row * 14 + i;
      const bool inSel = (p >= pos && p < pos + width);
      if (inSel) {
        lcd.write(byte(5));
        continue;
      }
      lcd.write(is_position_occupied(p) ? byte(4) : byte(6));
    }
  }
}

int request_dispenser_placement(int disp_id, int brick_id) {
  ui_state = UIState::PRINTING_ACTIVE;

  const int width = get_dispensor_width(disp_id);
  int pos = 0;
  while (pos < 28 && !is_legal_placement(pos, width)) pos++;
  if (pos >= 28) {
    display_message(F("No placement"), 800);
    ui_state = UIState::PRINTING_PASSIVE;
    return -1;
  }

  

  render_placement(brick_id, pos, width);

  while (!killTriggered) {
    (void)process_event();

    const int8_t d = read_encoder_delta();
    if (d != 0) {
      int next = pos + d;
      if (next < 0) next = 27;
      if (next > 27) next = 0;

      int guard = 0;
      while (guard++ < 28 && !is_legal_placement(next, width)) {
        next += d;
        if (next < 0) next = 27;
        if (next > 27) next = 0;
      }
      if (is_legal_placement(next, width)) {
        pos = next;
        render_placement(brick_id, pos, width);
      }
    }

    if (button_pressed_edge()) {
      ui_state = UIState::PRINTING_PASSIVE;
      return pos;
    }

    if (digitalRead(killBtn) == LOW) {
      killTriggered = true;
      break;
    }
    delay(2);
  }

  ui_state = UIState::PRINTING_PASSIVE;
  return -1;
}

// -----------------------------
// Startup screen
// -----------------------------
void showProStartup() {
  lcd.clear();
  applyLCDTheme(1);
  lcd.setCursor(4, 1);
  lcd.print("MKLEGO");
  lcd.setCursor(1, 2);
  lcd.print("Initializing...");
  delay(600);
}
