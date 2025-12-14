#ifndef PRINT_ENGINE_H
#define PRINT_ENGINE_H

#include <SD.h>

void runLGCodeFromSD(String filename);

void playSongFromSD(String filename);
int getNoteFrequency(String note);

void buzz(int pin, long freq, long len);
void playMelody(const uint8_t *notes, const uint8_t *durations, int len);

void playFinishBeep();

#endif