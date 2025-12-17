#include "ui.h"
#include "physics.h"
#include "print_engine.h"
#include "melodies.h"
#include "dispensor.h"

//brick_type bricks[MAX_NUMBER_DISPENSORS] = {{2, 2, 0, 1}, {2, 2, 0, 2}, {3, 2, 0, 3}, {4, 2, 0, 4}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
//uint8_t    brick_resistances[MAX_NUMBER_DISPENSORS] = {1, 2, 3, 4, 5, 6, 7};
//brick_type types[4] = {{2, 2, 0, 1}, {2, 2, 0, 2}, {3, 2, 0, 3}, {4, 2, 0, 4}};


// small icons
byte playIcon[8]  = {B00000,B01000,B01100,B01110,B01100,B01000,B00000,B00000};
byte folderIcon[8] = {B01100, B11110,B10010,B10010,B10010,B10010,B11110,B00000}; // folder-like
byte settingsIcon[8] = {B00100,B01110,B11111,B01010,B11111,B01110,B00100,B00000};
byte homeIcon[8]  = {B00100,B01010,B11111,B10001,B10001,B10001,B10001,B11111};

byte barEmpty[8]  = {0,0,0,0,0,0,0,0};
byte barHalf[8]   = {0,0,0,0,0,31,31,31};
byte barFull[8]   = {31,31,31,31,31,31,31,31};

byte load0[8] = {0,0,0,0,0,0,0,0};
byte load1[8] = {B11111,0,0,0,0,0,0,0};
byte load2[8] = {B11111,B11111,0,0,0,0,0,0};
byte load3[8] = {B11111,B11111,B11111,0,0,0,0,0};
byte load4[8] = {B11111,B11111,B11111,B11111,0,0,0,0};
byte load5[8] = {B11111,B11111,B11111,B11111,B11111,0,0,0};

byte placedBlockIcon[8] = {B01110,B01110,B01110,B01110,B01110,B01110,B01110,B01110};
byte hollowBlockIcon[8] = {B01110,B01010,B01010,B01010,B01010,B01010,B01010,B01110};
byte placingIcon[8] = {B11111,B11111,B11111,B11111,B11111,B11111,B11111,B11111};

LiquidCrystal lcd(RS_PIN, EN_PIN, D4_PIN, D5_PIN, D6_PIN, D7_PIN);

const int buzzerPin    = 28;
const int encA         = 46;
const int encB         = 48;
const int encBtn       = 29;
const int killBtn      = 49;
const int chipSelect   = 53;
const int lcdBacklight = 47;
const int sdDetectPin  = 44;

// === UI state variables ===
int lastA = HIGH;
int appState = 0; // 0 = main menu, 1 = SD browser, 2 = settings, 4 = dispensor setup
int menuIndex = 0;
int printIndex = 0;              // 0 = folder (Files), 1 = home (Back)
int fileIndex = 0;
int settingsIndex = 0;
int fileCount = 0;
int lcdTheme = 1;  // 0=dim, 1=bright, 2=pulse (temporary)
String fileNames[20];
int controlIndex = 0;
int dispensorState = 0; //0=checking, 1=not found, 2=placing
int dispensorPos = 0;
int dispensorWidth = 0;
int dispensorIndex = 0;
int typeIndex = 0;

volatile bool killTriggered = false;

bool silentMode = false; 

unsigned long lastActivity = 0;
bool screensaverActive = false;

unsigned long btnPressTime = 0;
bool btnHeld = false;
unsigned long printStartMillis = 0;

// drifting logo position
int ss_x = 0;
int ss_y = 0;
int ss_dx = 1;
int ss_dy = 1;

//knob rotation positions 
int oldPos = 0;
int curPos = 0;

// -----------------------------
// INIT
// -----------------------------
void setupUI() {
    pinMode(encA, INPUT_PULLUP);
    pinMode(encB, INPUT_PULLUP);
    pinMode(encBtn, INPUT_PULLUP);
    pinMode(killBtn, INPUT_PULLUP);
    pinMode(buzzerPin, OUTPUT);
    pinMode(lcdBacklight, OUTPUT);

    analogWrite(lcdBacklight, 180);  // initial brightness

    // Kill button interrupt
    // attachInterrupt(digitalPinToInterrupt(killBtn), killISR, FALLING); // for now, we use just the reset pin on the arduino mega

    lastA = digitalRead(encA);
}

void setupLCD() {
  lcd.begin(20, 4);
  lcd.createChar(0, playIcon);
  lcd.createChar(1, folderIcon);
  lcd.createChar(2, settingsIcon);
  lcd.createChar(3, homeIcon);
  lcd.createChar(4, barEmpty);
  lcd.createChar(5, barHalf);
  lcd.createChar(6, barFull);
}
void setupSD() {
    if (!SD.begin(chipSelect)) {
        lcd.clear();
        lcd.print("SD init failed!");
        tone(buzzerPin, 400, 400);
        delay(800);
    }
}

// -----------------------------
// UI FUNCTIONS (EMPTY BODIES)
// -----------------------------
// -----------------------------
// === Encoder & UI functions ===
void handleEncoder(){
  int currentA = digitalRead(encA);
  if (currentA != lastA) {
    lastActivity = millis();
    if (screensaverActive) exitScreensaver();
    if (digitalRead(encB) != currentA) {
      curPos++;
      if(curPos - oldPos >= 2){
        if (!silentMode) tone(buzzerPin, 1200, 40);
        if (appState == 0) menuIndex++;
        else if (appState == 1) fileIndex++;
        //else if (appState == 2) menuIndex++; 
        else if (appState == 3) printIndex++;
        else if (appState == 2) settingsIndex++;
        //else if (appState == 98 && dispensorState == 2) dispensorPos++;
        oldPos = curPos;
      }
      
    } else {
      curPos--;
      if(oldPos - curPos >= 2){
        if (!silentMode) tone(buzzerPin, 1200, 40);
        if (appState == 0) menuIndex--;
        else if (appState == 1) fileIndex--;
        //else if (appState == 2) menuIndex--; 
        else if (appState == 3) printIndex--;
        else if (appState == 2) settingsIndex--;
        //else if (appState == 98 && dispensorState == 2) dispensorPos--;
        oldPos = curPos;
      }
    
    }
    constrainIndices();
    if (appState == 0) showMainMenu();
    if (appState == 1 && !screensaverActive) showFiles();
    if (appState == 2) showSettingsmenu();
    if (appState == 3) showPrintMenu();
   //if (appState == 98) showDispensorMenu();
    delay(5);
  }
  lastA = currentA;
}

void handleButtons() {

  int btn = digitalRead(encBtn);

  // ---------------------------
  // Detect button press start
  // ---------------------------
  if (btn == LOW && !btnHeld) {
    btnHeld = true;
    btnPressTime = millis();
  }

  // ---------------------------
  // Detect button release
  // ---------------------------
  if (btn == HIGH && btnHeld) {
    unsigned long pressDuration = millis() - btnPressTime;
    btnHeld = false;

    // ---- SHORT PRESS (< 3 sec) ----
    if (pressDuration < 3000) {
      lastActivity = millis();
      if (screensaverActive) exitScreensaver();

      if (!silentMode) {
        tone(buzzerPin, 2000, 80);
        delay(150);
        noTone(buzzerPin);
      } else {
        delay(150);
      }

      // === ORIGINAL SHORT-PRESS LOGIC ===
      if (appState == 0) openMenu(menuIndex);
      else if (appState == 1) selectFile();
      else if (appState == 2) { 
        if (settingsIndex == 0) {
            silentMode = !silentMode;
            showSettingsmenu();
        }
        else if (settingsIndex == 1) {
            showCalibrateMenu();
        }
        else if (settingsIndex == 2) {
            appState = 0;
            showMainMenu();
        }
      }
      else if (appState == 3) {
        if (printIndex == 0) {
          listFiles();
          appState = 1;
          fileIndex = 0;
          showFiles();
        } else {
          appState = 0;
          showMainMenu();
        }
      }
/*       else if(appState == 98){
        if (dispensorState == 1) {
          startDispenserMenu();
        } else if (dispensorState == 2) {
          set_dispensor_state(dispensorIndex, false);
          set_dispensor_pos(dispensorIndex, dispensorPos);
          if(is_legal_placement(dispensorPos, dispensorWidth)){
            lcd.clear();
            lcd.setCursor(0, 1);
            lcd.print("Dispenser placed!");
            delay(800);
            if(++dispensorIndex>=MAX_NUMBER_DISPENSORS) {
              appState = 99;
              dispensorIndex = 0;
              runLGCodeFromSD(fileNames[fileIndex]);
            }
            else {
              dispensorPos = 0;
              //showDispensorMenu();
            };
          } else {
            lcd.clear();
            lcd.setCursor(0, 1);
            lcd.print("Illegal placement!");
            delay(800);
            dispensorPos = 0;
            //showDispensorMenu();
          }
        }
      } */
      delay(200);
    }
  }

  // ---------------------------------
  // LONG PRESS (>= 3s) → GO HOME
  // ---------------------------------
  if (btnHeld && (millis() - btnPressTime >= 3000)) {

    btnHeld = false;  // prevent multiple triggers

    // feedback
    if (!silentMode) tone(buzzerPin, 1200, 300);
    delay(200);

    lcd.clear();
    lcd.print("Returning Home...");
    delay(800);

    appState = 0;
    menuIndex = 0;
    showMainMenu();
    return;
  }

  // ---------------------------------
  // Kill button (unchanged)
  // ---------------------------------
  if (digitalRead(killBtn) == LOW) {
    if (!silentMode) tone(buzzerPin, 400, 200);
    killTriggered = true;
    appState = 0;
    showMainMenu();
    delay(300);
  }
}

void constrainIndices(){
  // main menu: two items (Print=0, Settings=1)
  if (menuIndex < 0) menuIndex = 1;
  if (menuIndex > 1) menuIndex = 0;

  if (fileIndex < 0) fileIndex = fileCount - 1;
  if (fileIndex >= fileCount) fileIndex = 0;

  if (printIndex < 0) printIndex = 1;
  if (printIndex > 1) printIndex = 0;
  
  if (settingsIndex < 0) settingsIndex = 2;
  if (settingsIndex > 2) settingsIndex = 0;

  if (dispensorPos < 0) dispensorPos = 28 - dispensorWidth;
  if (dispensorPos > 28 - dispensorWidth) dispensorPos = 0;
  
}

void showMainMenu(){
  lcd.clear();
  applyLCDTheme(1);

  // Header
  lcd.setCursor(7,0);
  lcd.write(byte(3));   // Home icon
  lcd.print(" Home");

  // Print line
  lcd.setCursor(0,1);
  lcd.write(byte(0));   // play icon
  lcd.print("  Print");

  // Settings line
  lcd.setCursor(0,2);
  lcd.write(byte(2));   // settings icon
  lcd.print("  Settings");

  // Selector arrow
  lcd.setCursor(18, menuIndex+1);
  lcd.print("<");
}

void openMenu(int index){
  lcd.clear();
  switch(index){
    case 0:
      // show small submenu: left -> Files, right -> Home
      appState = 3;
      printIndex = 0;
      showPrintMenu();
      break;
    case 1:
       appState = 2;
       showSettingsmenu();
       break;
  }
}

void listFiles(){
  fileCount = 0;
  if (!SD.begin(chipSelect)) {
    lcd.clear(); lcd.print("SD init failed!");
    if (!silentMode) tone(buzzerPin, 400, 400);
    delay(800);
    appState = 0;
    return;
  }

  File root = SD.open("/");
  while (true) {
    File entry = root.openNextFile();
    if (!entry) break;
    if (!entry.isDirectory()) {
      String name = String(entry.name());
      // filter hidden/system files; only accept .txt / .lgcode / .lg
      if (name.startsWith(".") || name.startsWith("_")) { entry.close(); continue; }
      String upper = name;
      upper.toUpperCase();
      if (!upper.endsWith(".TXT") && !upper.endsWith(".lgcode") && !upper.endsWith(".LG")) {
        entry.close();
        continue;
      }
      fileNames[fileCount++] = name;
      if (fileCount >= 20) { entry.close(); break; }
    }
    entry.close();
  }
  root.close();

  if (fileCount == 0) fileNames[fileCount++] = "No TXT found";
  fileIndex = 0;
}

void showFiles(){
  lcd.clear();
  lcd.setCursor(0,0);
  lcd.print("Files on SD:");
  // top-left folder icon + home icon (allow immediate return)
  //lcd.setCursor(17,0);
  //lcd.write(byte(1)); // folder icon for aesthetics
  //lcd.setCursor(19,0);
  //lcd.write(byte(3)); // home icon small

  for (int i = 0; i < 3 && i < fileCount; i++) {
    int idx = (fileIndex + i) % fileCount;
    lcd.setCursor(0, i+1);
    lcd.print((idx == fileIndex) ? "> " : "  ");
    lcd.print(fileNames[idx]);
  }
}

void selectFile(){
  lastA = digitalRead(46);  // reset encoder state
  lcd.clear();
  lcd.print("Selected:");
  lcd.setCursor(0,1);
  lcd.print(fileNames[fileIndex]);
  tone(buzzerPin, 2000, 100);
  delay(200);
  
  String filename = fileNames[fileIndex];
  filename.trim();
  String upper = filename;
  upper.toUpperCase();

  if (upper.endsWith(".TXT")) {
    playSongFromSD(filename);
  }
  else if (upper.endsWith(".LGCODE") || upper.endsWith(".LG")) {
    runLGCodeFromSD(filename);
    return;
  }
  else {
    lcd.clear();
    lcd.print("Unsupported file!");
  }
  showFiles();
}


void showControlsMenu() {
  lcd.clear();
  lcd.setCursor(0,0); 
  lcd.print("Controls:");

  lcd.setCursor(0,1); lcd.print((controlIndex == 0) ? "> X axis" : "  X axis");
  lcd.setCursor(0,2); lcd.print((controlIndex == 1) ? "> Y axis" : "  Y axis");
  lcd.setCursor(0,3); lcd.print((controlIndex == 2) ? "> Z axis" : "  Z axis");
  
  lcd.setCursor(10,1); lcd.print((controlIndex == 3) ? F("> Servo") : F(" Servo"));
  lcd.setCursor(10,2); lcd.print((controlIndex == 4) ? "> Rotate" : "  Rotate");
}

void openControlsItem(int index) {
  lastA = digitalRead(46);  // reset encoder state
  while (digitalRead(encBtn) == LOW) { delay(5); }
  lcd.clear();

  switch(index){
    case 0:
      //controlAxis("X");
      break;

    case 1:
     // controlAxis("Y");
      break;

    case 2:
      //controlAxis("Z");
      break;

    case 3:
      //toggleServo();
      break;

    case 4:
      //toggleRotation();
      break;
  }

  showControlsMenu();
}
/*void controlAxis(String axis) {
 // long pos = 0;

  //lcd.clear();
  //lcd.print(axis + " adjust");

  //while(digitalRead(encBtn) == HIGH){   // exit when button pressed
   // int lastLocalA = digitalRead(encA);

    while(digitalRead(encBtn) == HIGH) {

        int currentA = digitalRead(encA);

        if (currentA != lastLocalA) {
            if (digitalRead(encB) != currentA) pos++;
            else pos--;

            lcd.setCursor(0,1);
            lcd.print(F("Pos: "));
            lcd.print(pos);
            lcd.print("   ");
        }

        lastLocalA = currentA;
    }
  }

  // Move axis when exiting
  if(axis == "X") moveX(pos);
  if(axis == "Y") moveY(pos);
  if(axis == "Z") moveZ(pos);

  showControlsMenu();
}*/

/* void startDispenserMenu() {
  bool found = false;
  typeIndex = 0;
  for (size_t i = 0; i < get_number_types(); i++, typeIndex++) {
    found = false;
    dispensorIndex = 0;
    for (size_t j = 0; j < MAX_NUMBER_DISPENSORS; j++, dispensorIndex++) {
      if (get_dispensor(dispensorIndex) == get_types_uuids()[i]) {
        found = true;
        showBrickFoundMessage();
        break;
      }
//      if (bricks[j].resistor == get_type(get_types_uuids()[i])->resistor) {
//        found = true;
//        showBrickFoundMessage();
//        break;
//      }
    }
    if (!found) {
      dispensorState = 1;
      showDispensorMissingMessage();
      return;
    }
  }
  dispensorIndex = 0;
  dispensorState = 2;
  showDispensorMenu();
} */

void showDispensorMissingMessage(int type, int brick_id) { //0=no dispensor 1=empty dispensor
lcd.clear();
  lcd.setCursor(0, 0);
  type == 0 ? lcd.print("Add the ") : lcd.print("Charge the ");
  lcd.setCursor(0, 1);
  lcd.print(get_type(brick_id)->size_x);
  lcd.print("x");
  lcd.print(get_type(brick_id)->size_y);
  lcd.print(" ");
  char* color = get_color(get_type(brick_id)->color)->name;
  for (int i = 0; i <= COLOR_NAME_MAX_SIZE; ++i) {
    if (color[i] == '\0') break;
    else lcd.print(color[i]);
  }
  lcd.setCursor(0, 2);
  lcd.print("Dispenser(s)! >:(");
  delay(800);
}

void showBrickFoundMessage(int brick_id) {
  lcd.clear();
  lcd.setCursor(0, 1);
  lcd.print("Found Dispenser for:");
  lcd.setCursor(0, 2);
  lcd.print(get_type(brick_id)->size_x);
  lcd.print("x");
  lcd.print(get_type(brick_id)->size_y);
  lcd.print(" ");
  char* color = get_color(get_type(brick_id)->color)->name;
  for (int i = 0; i <= COLOR_NAME_MAX_SIZE; ++i) {
    if (color[i] == '\0') break;
    else lcd.print(color[i]);
  }
  delay(800);
}

void handleEncoderDispensorMenu(int disp_id, int brick_id){
  int currentA = digitalRead(encA);
  if (currentA != lastA) {
    lastActivity = millis();
    if (digitalRead(encB) != currentA) {
      curPos++;
      if(curPos - oldPos >= 2){
        if (!silentMode) tone(buzzerPin, 1200, 40);
        dispensorPos++;
        oldPos = curPos;
      }
    } else {
      curPos--;
      if(oldPos - curPos >= 2){
        if (!silentMode) tone(buzzerPin, 1200, 40);
        dispensorPos--;
        oldPos = curPos;
      }
    }
    constrainIndices();
    showDispensorMenu(disp_id, brick_id);
    delay(5);
  }
  lastA = currentA;
}

void handleButtonsDispensorMenu(int disp_id, int brick_id) {

  int btn = digitalRead(encBtn);

  // ---------------------------
  // Detect button press start
  // ---------------------------
  if (btn == LOW && !btnHeld) {
    btnHeld = true;
    btnPressTime = millis();
  }

  // ---------------------------
  // Detect button release
  // ---------------------------
  if (btn == HIGH && btnHeld) {
    unsigned long pressDuration = millis() - btnPressTime;
    btnHeld = false;

    // ---- SHORT PRESS (< 3 sec) ----
    if (pressDuration < 3000) {
      lastActivity = millis();

      if (!silentMode) {
        tone(buzzerPin, 2000, 80);
        delay(150);
        noTone(buzzerPin);
      } else {
        delay(150);
      }
    }
    if(is_legal_placement(dispensorPos, dispensorWidth)){
      dispensorPos = 0;
      dispensorWidth = 0;
      appState = 99;
      lcd.clear();
      lcd.setCursor(0, 1);
      lcd.print("Dispenser placed!");
      delay(800);
    } else {
      lcd.clear();
      lcd.setCursor(0, 1);
      lcd.print("Illegal placement!");
      delay(800);
      dispensorPos = 0;
      showDispensorMenu(disp_id, brick_id);
    }
  }
  delay(200);
}

void showDispensorMenu(int disp_id, int brick_id) {
/*   while (dispensorIndex < MAX_NUMBER_DISPENSORS && get_dispensor(dispensorIndex)->brick_id == -1) {
    dispensorIndex++;
  }
  if (dispensorIndex >= MAX_NUMBER_DISPENSORS) {
      appState = 99;
      runLGCodeFromSD(fileNames[fileIndex]);
      return;
  } */
  dispensorIndex = disp_id;
  dispensorWidth = get_dispensor_width(disp_id);
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print(get_type(brick_id)->size_x);
  lcd.print("x");
  lcd.print(get_type(brick_id)->size_y);
  lcd.print(" ");
  char* color = get_color(get_type(brick_id)->color)->name;
  for (int i = 0; i <= COLOR_NAME_MAX_SIZE; ++i) {
    if (color[i] == '\0') break;
    else lcd.print(color[i]);
  }
  lcd.print(" Pos ");
  lcd.print(dispensorPos);
  bool legal = is_legal_placement(dispensorPos, dispensorWidth);
  print_row_dispensors(1, legal);
  print_row_dispensors(2, legal);
}

void print_row_dispensors(int row, bool legal) {
  lcd.createChar(7, placedBlockIcon);
  lcd.createChar(8, hollowBlockIcon);
  lcd.setCursor(0, row);
  for (size_t i = 14*(row - 1); i < 14*row; i++){
    if (i >= dispensorPos && i < dispensorPos + dispensorWidth) {
      if (legal) {
        lcd.write(byte(6));
      } else {
        lcd.print("X");
      }
    } else {
      if (is_legal_placement(i, 1)){
        lcd.write(byte(8));
      } else {
        lcd.write(byte(7));
      }
    }
  }
}

void handleScreensaver() {
  // Don't run screensaver ONLY while printing
  if (appState == 99 || appState == 98) return;   // special "printing" state

  unsigned long now = millis();

  // Activate screensaver after inactivity (15s)
  if (!screensaverActive && (now - lastActivity > 15000)) {
    startScreensaver();
  }

  // If screensaver active → animate drifting logo
  if (screensaverActive) {
    showStartupScreensaver();
  }
}

void applyLCDTheme(int mode) {
  if (mode == 0) {              // DIM
    analogWrite(lcdBacklight, 120);
  }
  else if (mode == 1) {         // BRIGHT
    analogWrite(lcdBacklight, 255);
  }
  else if (mode == 2) {         // PULSE FLASH
    analogWrite(lcdBacklight, 255);
    delay(120);
    analogWrite(lcdBacklight, 80);
    delay(120);
    analogWrite(lcdBacklight, 255);
    delay(120);
  }
}

void startScreensaver() {
    screensaverActive = true;
    lcd.clear();
    ss_x = 0; ss_y = 1;
    ss_dx = 1; ss_dy = 1;
}

void exitScreensaver() {
    screensaverActive = false;
    lcd.clear();
    showMainMenu();
}

void showStartupScreensaver() {
   lcd.clear();

    // Starting position
    int x = random(0, 20 - 6);   // "MKLEGO" is 6 chars
    int y = random(0, 4);

    int dx = 1;
    int dy = 1;

    unsigned long lastMove = millis();

    while (screensaverActive) {

        // move text every 1000 ms
        if (millis() - lastMove > 1000) {
            lastMove = millis();

            // clear previous
            lcd.clear();

            // draw moving text
            lcd.setCursor(x, y);
            lcd.print("MKLEGO");

            // bounce horizontally
            x += dx;
            if (x <= 0 || x >= 20 - 6) dx = -dx;

            // bounce vertically
            y += dy;
            if (y <= 0 || y >= 3) dy = -dy;
        }

        // --- Exit condition: any encoder activity ---
        int a = digitalRead(encA);
        int btn = digitalRead(encBtn);

        if (a != lastA || btn == LOW) {
            screensaverActive = false;
            lcd.clear();
            lastActivity = millis();
            return;
        }
    }
}
void preheatAnimation() {
  lcd.clear();
  lcd.setCursor(0,1); 
  lcd.print("Preparing...");

  byte* loadFrames[] = {load0, load1, load2, load3, load4, load5};

  for (int i = 0; i < 20; i++) {
    int frame = i % 6;
    lcd.createChar(7, loadFrames[frame]); // store animation frame in slot 7
    lcd.setCursor(i, 2);
    lcd.write(byte(7));
    delay(100);
  }
}
void showProStartup() {
    lcd.clear();
    applyLCDTheme(1); // full backlight

    // ---- 1. MKLEGO logo fade-in ----
    lcd.setCursor(5,1);
    lcd.print("MKLEGO");

    for (int b = 40; b <= 255; b += 15) {
        analogWrite(lcdBacklight, b);
        delay(40);
    }
    delay(400);

    // ---- 2. "Initializing..." ----
    lcd.setCursor(3,2);
    lcd.print("Initializing...");
    delay(500);

    // ---- 3. System checks ----
    lcd.clear();
    lcd.print(" System Check");

    lcd.setCursor(0,1); lcd.print("LCD......OK");
    delay(250);
    lcd.setCursor(0,2); lcd.print("SD.......");
    delay(100);

    if (SD.begin(chipSelect)) {
      lcd.print("OK");
    } else {
      lcd.print("NO SD");
    }

    delay(250);
    lcd.setCursor(0,3); lcd.print("Motors...OK");
    delay(250);

    // ---- 4. Loading progress bar ----
    lcd.clear();
    lcd.print("Loading UI...");
    lcd.setCursor(0,2);

    for (int i = 0; i < 20; i++) {
        lcd.write(byte(15));   // fill block
        delay(80);
    }

    // ---- 5. Fade-out + Ready ----
    for (int b = 255; b >= 120; b -= 15) {
        analogWrite(lcdBacklight, b);
        delay(30);
    }

    lcd.clear();
    lcd.setCursor(4,1);
    lcd.print("READY!");
    delay(900);

    applyLCDTheme(1); // max brightness
}
void drawProgressBar(int percent) {
  int totalBlocks = 20;   // LCD width
  float p = percent / 100.0 * totalBlocks;

  int full = (int)p;
  bool hasHalf = (p - full) >= 0.5;

  lcd.setCursor(0,3);

  // Full blocks
  for (int i = 0; i < full; i++)
    lcd.write(byte(6));  // full

  // Half block
  if (hasHalf) {
    lcd.write(byte(5));
    full++;
  }

  // Empty blocks
  for (int i = full; i < totalBlocks; i++)
    lcd.write(byte(4));
}
void showCalibrateMenu(){
  calibrateAll();

  showSettingsmenu();
}
void showPrintMenu() {
  lcd.clear();
  applyLCDTheme(1);

  // Draw left folder icon and right home icon
  // Left side label
  lcd.setCursor(0,1);
  lcd.write(byte(0));
  lcd.print("  Files");

  // Right side home icon
  lcd.setCursor(12,1);
  lcd.write(byte(3)); // homeIcon
  lcd.print(" Back");

  // Draw a simple selector under the current choice
  if (printIndex == 0) {
    // show arrow under left
    lcd.setCursor(2,2);
    lcd.print("^");
    // clear right arrow
    lcd.setCursor(14,2);
    lcd.print(" ");
  } else {
    // show arrow under right
    lcd.setCursor(14,2);
    lcd.print("^");
    lcd.setCursor(2,2);
    lcd.print(" ");
  }

}
void showSettingsmenu(){
  lcd.clear();
  lcd.setCursor(0,0);
  lcd.print("Settings:");

  // Silent Mode
  lcd.setCursor(0,1);
  lcd.print((settingsIndex == 0) ? "> Silent Mode: " : "  Silent Mode: ");
  lcd.print(silentMode ? "ON " : "OFF");

  // Calibrate
  lcd.setCursor(0,2);
  lcd.print((settingsIndex == 1) ? "> Calibrate" : "  Calibrate");

  // Back
  lcd.setCursor(0,3);
  lcd.print((settingsIndex == 2) ? "> Back" : "  Back");
}
