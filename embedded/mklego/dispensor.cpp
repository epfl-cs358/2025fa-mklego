
#include <stddef.h>
#include "lgcode.h"
#include "dispensor.h"

static dispensor fx_dispensors[MAX_NUMBER_DISPENSORS];
static bool      positions[28]; //true when occupied

// --- Dispensor getters ---
const dispensor* get_dispensor (int dispensor_id) {
    return (const dispensor*) (fx_dispensors + dispensor_id);
}

int get_dispensor_width (int dispensor_id) {
    return get_type(fx_dispensors[dispensor_id].brick_id)->size_x + 1;
}

int find_non_empty_dispensor_with_brick (int brick_id) {
    if (brick_id == -1) return -1;
    for (size_t i = 0; i < MAX_NUMBER_DISPENSORS; i++) {
        bool foundEmpty = false;
        if (fx_dispensors[i].brick_id == brick_id) {
            return i;
        }
    }
    return -1;
}

// --- Dispensor setters ---
void set_dispensor_brick (int dispensor_id, int  brick_id) {
    if (brick_id == -1) fx_dispensors[dispensor_id].pos = -1;
    fx_dispensors[dispensor_id].brick_id = brick_id;
}

void set_dispensor_pos   (int dispensor_id, int  pos) {
    fx_dispensors[dispensor_id].pos = pos;
}

// --- Position Utils ---
bool is_legal_placement (int pos, int width) {
    if (pos < 0 || pos + width > 28) return false;
    for (size_t i = 0; i < width; i++) {
        if (positions[i + pos] && get_dispensor(i)->brick_id != -1) return false;
    }
    return true;
}
