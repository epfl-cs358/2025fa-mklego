#ifndef COMMUNICATION_H
#define COMMUNICATION_H

#include <Arduino.h>

void communication_begin(int sck, int dat, int avl);

int master_read_bit();
unsigned char master_read_byte();
unsigned char master_read_byte_safe();

bool master_read(unsigned char *buffer, int size);
bool process_event();

#endif
