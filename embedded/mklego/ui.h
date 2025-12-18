#ifndef UI_H
#define UI_H

#include <Arduino.h>
#include <LiquidCrystal.h>

enum class UIState : uint8_t {
  MENU_MAIN,
  MENU_FILES,
  MENU_SETTINGS,
  PRINTING_PASSIVE,
  PRINTING_ACTIVE,
};

// === LCD pins ===
#define RS_PIN 27
#define EN_PIN 26
#define D4_PIN 24
#define D5_PIN 25
#define D6_PIN 22
#define D7_PIN 23

extern LiquidCrystal lcd;
extern volatile bool killTriggered;

extern volatile UIState ui_state;


extern const int buzzerPin;
extern const int encA;
extern const int encB;
extern const int encBtn;
extern const int killBtn;
extern const int chipSelect;
extern const int lcdBacklight;
extern const int sdDetectPin;

// INIT
void setupUI();
void setupLCD();
void setupSD();

// UI / ENCODER
int8_t read_encoder_delta();
bool button_pressed_edge();
void wait_for_button();
void constrainIndices();

// State machine
void ui_set_state(UIState state);
void handle_main_menu();
void handle_file_browser();
void handle_settings_menu();

// MENUS / RENDER
void showMainMenu();
void showFiles();
void showSettingsmenu();
void listFiles();

// Printing/dispenser prompts (blocking, PRINTING_ACTIVE)
void display_message(const __FlashStringHelper* msg, unsigned long duration_ms);
void display_message(const String& msg, unsigned long duration_ms);
int request_dispenser_placement(int disp_id, int brick_id);
bool request_add_dispenser(int brick_id);
bool request_refill_dispenser(int disp_id, int brick_id);

// LCD / THEMES
void applyLCDTheme(int mode);

// Startup
void showProStartup();

#endif