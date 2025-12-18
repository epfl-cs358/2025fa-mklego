#include <AccelStepper.h>
#include <MultiStepper.h>
#include <Servo.h>
#include <LiquidCrystal.h>
#include <SPI.h>
#include <SD.h>
#include "lgcode.h"
#include "pitches.h"
#include "melodies.h"
#include "physics.h"
#include "dispensor.h"
#include "ui.h"
#include "print_engine.h"
#include "communication.h"

void setup(){
    Serial.begin(9600);
    communication_begin(10, 11, 12);

    setupUI();
    setupLCD();
    setupSD();
    initPhysics();
    showProStartup();

    ui_set_state(UIState::MENU_MAIN);
}

void loop(){
  switch (ui_state) {
    case UIState::MENU_MAIN:
      handle_main_menu();
      break;
    case UIState::MENU_FILES:
      handle_file_browser();
      break;
    case UIState::MENU_SETTINGS:
      handle_settings_menu();
      break;
    case UIState::PRINTING_PASSIVE:
    case UIState::PRINTING_ACTIVE:
      break;
  }
}