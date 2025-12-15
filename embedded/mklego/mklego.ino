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
#include "dispenser.h"
#include "ui.h"
#include "print_engine.h"

// -----------------------------
// === Setup ===
void setup(){
    Serial.begin(9600);

    reset_lgcode();

    //setupMovement();
    setupUI();
    setupLCD();
    //setupSD();
    initPhysics();
    showProStartup();
    // startup -> normal state
    screensaverActive = false;
    lastActivity = millis();
    showMainMenu();
}
// -----------------------------
// === Main loop: handles both systems ===
void loop(){
  //handleSerialCommands();  // movement via serial 
  handleEncoder();         // UI navigation (encoder)
  handleButtons();         // encoder press and other buttons
  handleScreensaver();  
}