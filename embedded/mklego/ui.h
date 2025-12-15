#ifndef UI_H
#define UI_H

#include <LiquidCrystal.h>
#include <Arduino.h>

// === LCD pins ===
#define RS_PIN 27
#define EN_PIN 26
#define D4_PIN 24
#define D5_PIN 25
#define D6_PIN 22
#define D7_PIN 23

// small icons
extern byte playIcon[8];
extern byte folderIcon[8];
extern byte settingsIcon[8];
extern byte homeIcon[8];

extern byte barEmpty[8];
extern byte barHalf[8];
extern byte barFull[8];

//loading bar preheat
extern byte load0[8] ;
extern byte load1[8] ;
extern byte load2[8] ;
extern byte load3[8] ;
extern byte load4[8] ;
extern byte load5[8] ;

extern byte placedBlockIcon[8] ;
extern byte hollowBlockIcon[8] ;
extern byte placingIcon[8] ;

extern LiquidCrystal lcd;
extern volatile bool killTriggered;


extern const int buzzerPin;
extern const int encA;
extern const int encB;
extern const int encBtn;
extern const int killBtn;
extern const int chipSelect;
extern const int lcdBacklight;
extern const int sdDetectPin;

// === UI state variables ===
extern int lastA;
extern int appState; // 0 = main menu, 1 = SD browser, 2 = settings, 4 = dispenser setup
extern int menuIndex;
extern int printIndex;              // 0 = folder (Files), 1 = home (Back)
extern int fileIndex;
extern int settingsIndex;
extern int fileCount;
extern int lcdTheme;  // 0=dim, 1=bright, 2=pulse (temporary)
extern String fileNames[20];
extern int controlIndex;
extern int dispenserPos;
extern int dispenserWidth;
extern int dispenserIndex;
extern brick_type bricks[MAX_NUMBER_DISPENSERS];

extern volatile bool killTriggered;

//has the current state of the printer (silent or not)
extern bool silentMode;

extern unsigned long lastActivity;
extern bool screensaverActive;

//long press button
extern unsigned long btnPressTime;
extern bool btnHeld;
//le temps ou l'impression a commence pour voir apres combien de temps ca a pris pour imprimer
extern unsigned long printStartMillis;

// drifting logo position
extern int ss_x;
extern int ss_y;
extern int ss_dx;
extern int ss_dy;

//knon rotation positions
extern int oldPos;
extern int curPos;

// INIT
void setupUI();
void setupLCD();
void setupSD();

// UI / ENCODER
void handleEncoder();
void handleButtons();
void constrainIndices();

// MENUS
void showMainMenu();
void openMenu(int index);
void showFiles();
void selectFile();
void listFiles();

// CONTROLS MENU
void showControlsMenu();
void openControlsItem(int index);
void controlAxis(String axis);

// DISPENSER MENU
void showDispenserMenu();
void print_row_dispensers(int row, bool legal);

//Settings Menu
void showSettingsmenu();
void showCalibrateMenu();

//menu files-Back
void showPrintMenu();

// LCD / THEMES
void applyLCDTheme(int mode);

// SCREENSAVER
void handleScreensaver();
void startScreensaver();
void exitScreensaver();

//setup screensaver
void showProStartup();

//inactivity screensaver 
void showStartupScreensaver();

//preheat before printing
void preheatAnimation();


// UTIL
void drawProgressBar(int percent);

#endif