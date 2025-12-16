#ifndef MELODIES_H
#define MELODIES_H
#include <avr/pgmspace.h>

#include "pitches.h"

// Mario Theme
extern const uint8_t marioNotes[] PROGMEM;
extern const uint8_t marioDurations[] PROGMEM;
extern const uint8_t marioLength;


// Star Wars Theme
extern const uint8_t starWarsNotes[] PROGMEM;
extern const uint8_t starWarsDurations[] PROGMEM;
extern const uint8_t starWarsLength;


// Pirates of the Caribbean Theme
extern const uint8_t piratesNotes[] PROGMEM;
extern const uint8_t piratesDurations[] PROGMEM;
extern const uint8_t piratesLength;

#endif