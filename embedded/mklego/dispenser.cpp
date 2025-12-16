
#include <stddef.h>
#include "lgcode.h"
#include "dispenser.h"

const int WIDTH_2X2         = 3;
const int MAX_POSITION_2X2  = 25;
const int WIDTH_2X3         = 4;
const int MAX_POSITION_2X3  = 24;
const int WIDTH_2X4         = 5;
const int MAX_POSITION_2X4  = 23;

static dispenser    fx_dispensers[MAX_NUMBER_DISPENSERS];
static bool         positions[27]; //true when occupied

const dispenser* get_dispenser (brick_type* brick) {
    for (size_t i = 0; i < MAX_NUMBER_DISPENSERS; i++) {
        if (fx_dispensers[i].width) {
            if (fx_dispensers[i].brick.resistor == brick->resistor) {
                return (const dispenser*) (fx_dispensers + i);
            }
        }
    }
    return NULL;
}

bool is_legal_placement (int pos, int width) {
    if (pos < 0 || pos + width > 27) return false;
    for (size_t i = 0; i < width; i++) {
        if (positions[i + pos]) return false;
    }
    return true;
}

int remove_dispenser (dispenser disp) {
    int width = disp.width;
    int pos = disp.pos;
    for (size_t i = 0; i < MAX_NUMBER_DISPENSERS; i++) {
        if (fx_dispensers[i].width) {
            if (fx_dispensers[i].brick.resistor == disp.brick.resistor &&
                fx_dispensers[i].pos == disp.pos) {
                fx_dispensers[i].pos = -1;
                fx_dispensers[i].brick.resistor = 0;
                fx_dispensers[i].brick.color = 0;
                fx_dispensers[i].brick.size_x = 0;
                fx_dispensers[i].brick.size_y = 0;
                fx_dispensers[i].width = 0; //mark as removed
                for (size_t j = 0; j < width; j++) {
                    positions[j + pos] = false;
                }
                return i;
            }
        }
    }
    return -1; //dispenser not found
}

int place_dispenser (dispenser disp) {
    int width = disp.width;
    int pos = disp.pos;
    if (width < WIDTH_2X2 || width > WIDTH_2X4 || 
        !(is_legal_placement(pos, width))) 
        return -1;
    
    for (size_t i = 0; i < MAX_NUMBER_DISPENSERS; i++) {
        if (!fx_dispensers[i].width) {
            fx_dispensers[i] = disp;
            for (size_t j = 0; j < width; j++) {
                positions[j + pos] = true;
            }
            return i;
        }
    }
    return -1; //no space for the dispenser (must take one away at least)
}
