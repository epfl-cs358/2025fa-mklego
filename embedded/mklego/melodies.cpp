#include <avr/pgmspace.h>
#include "melodies.h"

// Mario Theme
const uint8_t marioNotes[] PROGMEM = {
  NOTE_E5, NOTE_E5, NOTE_E5, NOTE_C5,
  NOTE_E5, NOTE_G5, 0,        NOTE_G4,

  NOTE_C5, NOTE_G4, NOTE_E4, NOTE_A4,
  NOTE_B4, NOTE_AS4, NOTE_A6, 0,

  NOTE_E6, NOTE_G6, NOTE_A6, NOTE_F6,
  NOTE_G6, NOTE_E6, NOTE_C6, NOTE_D6,
  NOTE_B5, 0,

  NOTE_G6, NOTE_FS6, NOTE_F6, NOTE_D6,
  NOTE_E6, 0,        NOTE_A6, NOTE_A6,
  NOTE_A6, NOTE_F6, NOTE_G6, NOTE_F6,
  0,

  NOTE_E7, NOTE_E7, NOTE_E7, NOTE_C7,
  NOTE_E7, NOTE_G7, 0,        NOTE_G6,
  0
};

const uint8_t marioDurations[] PROGMEM = {
  12,12,12,12,
  12,12,12,12,

  12,12,12,12,
  12,12,12,12,

  12,12,12,12,
  12,12,12,12,
  12,12,

  12,12,12,12,
  12,12,12,12,
  12,12,12,12,
  12,

  12,12,12,12,
  12,12,12,12,
  12
};

const uint8_t marioLength PROGMEM = sizeof(marioNotes) / sizeof(marioNotes[0]);

// Star Wars Theme
const uint8_t starWarsNotes[] PROGMEM = {
  NOTE_A3, NOTE_D4, 0,
  NOTE_E4, NOTE_F4, NOTE_G4, NOTE_F4, 0,

  NOTE_A3, NOTE_A3, NOTE_D4,
  NOTE_E4, NOTE_F4, NOTE_A3, NOTE_D4,
  NOTE_A4, NOTE_G4,

  NOTE_A3, NOTE_D4, NOTE_E4, NOTE_F4,
  NOTE_D4, NOTE_A4, NOTE_F4, NOTE_D5,

  NOTE_D4, NOTE_F4, NOTE_E4, NOTE_D4,
  NOTE_A4, NOTE_F4, NOTE_D4,

  NOTE_A3, NOTE_A3, NOTE_A3, NOTE_D4,

  NOTE_A4, NOTE_D5, NOTE_E5,
  NOTE_F6, NOTE_G6, NOTE_F6,

  NOTE_A4, NOTE_A4, NOTE_D5,
  NOTE_E5, NOTE_F5, NOTE_D5, NOTE_A4,
  NOTE_A5, NOTE_G5,

  NOTE_A4, NOTE_D5, NOTE_E5, NOTE_F5,
  NOTE_D5, NOTE_A5, NOTE_F5, NOTE_D7,

  NOTE_D5, NOTE_F5, NOTE_E5, NOTE_D5,
  NOTE_A5, NOTE_F5, NOTE_D5,

  NOTE_A4, NOTE_A4, NOTE_A4, NOTE_D5
};

const uint8_t starWarsDurations[] PROGMEM = {
  16,25,12,
  16,12,12,25,12,

  16,12,25,
  12,12,12,12,
  12,32,

  16,25,12,12,
  12,12,12,32,

  12,12,12,12,
  16,12,12,

  16,8,12,40,

  16,25,16,
  12,12,25,

  16,12,25,
  12,12,12,12,
  12,40,

  16,16,12,12,
  12,12,12,32,

  12,12,12,12,
  16,12,12,

  16,8,8,70
};

const uint8_t starWarsLength PROGMEM  = sizeof(starWarsNotes) / sizeof(starWarsNotes[0]);

// Pirates of the Caribbean Theme
const uint8_t piratesNotes[] PROGMEM  = {
  NOTE_D4, NOTE_A3, NOTE_C4, NOTE_D4, NOTE_D4, NOTE_D4,
  NOTE_E4, NOTE_F4, NOTE_F4, NOTE_F4, NOTE_G4, NOTE_E4,
  NOTE_E4, NOTE_D4, NOTE_C4, NOTE_C4, NOTE_D4,

  NOTE_D4, NOTE_A3, NOTE_C4, NOTE_D4, NOTE_D4, NOTE_D4,
  NOTE_E4, NOTE_F4, NOTE_F4, NOTE_F4, NOTE_G4, NOTE_E4,
  NOTE_E4, NOTE_D4, NOTE_C4, NOTE_D4,

  NOTE_A3, NOTE_C3, NOTE_D4, NOTE_D4, NOTE_D4,
  NOTE_F4, NOTE_G4, NOTE_G4, NOTE_G4,
  NOTE_A4, NOTE_AS4, NOTE_AS4, NOTE_A4,
  NOTE_G4, NOTE_A4, NOTE_D4, NOTE_D4,
  NOTE_E4, NOTE_F4, NOTE_F4, NOTE_G4, NOTE_A4, NOTE_D4,

  NOTE_D4, NOTE_F4, NOTE_E4, NOTE_E4, NOTE_F4, NOTE_D4, NOTE_E4
};

const uint8_t piratesDurations[] PROGMEM = {
  12,12,12,12,12,12,
  12,12,12,12,12,12,
  16,12,8,8,16,

  12,12,12,12,12,12,
  12,12,12,12,12,12,
  16,12,8,20,

  12,12,12,12,12,
  12,12,12,12,
  12,12,12,12,
  12,12,12,12,
  12,12,12,12,12,16,

  12,12,12,12,12,12,12
};

const uint8_t piratesLength PROGMEM  = sizeof(piratesNotes) / sizeof(piratesNotes[0]);