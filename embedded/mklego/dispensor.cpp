
#include <stddef.h>
#include "lgcode.h"
#include "dispensor.h"

static dispensor fx_dispensors[MAX_NUMBER_DISPENSORS];
static bool      positions[28];

static void rebuild_positions() {
    for (size_t i = 0; i < 28; i++) {
        positions[i] = false;
    }
    for (size_t d = 0; d < MAX_NUMBER_DISPENSORS; d++) {
        const int pos = fx_dispensors[d].pos;
        if (pos < 0) continue;
        const int width = get_dispensor_width((int)d);
        if (width <= 0) continue;
        for (int i = 0; i < width; i++) {
            const int p = pos + i;
            if (p >= 0 && p < 28) positions[p] = true;
        }
    }
}

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
        if (fx_dispensors[i].brick_id == brick_id) {
            return i;
        }
    }
    return -1;
}

bool dispensor_is_empty(int dispensor_id) {
    if (dispensor_id < 0 || dispensor_id >= MAX_NUMBER_DISPENSORS) return true;
    return fx_dispensors[dispensor_id].status != 1;
}

bool is_position_occupied(int pos) {
    if (pos < 0 || pos >= 28) return false;
    return positions[pos];
}

void set_dispensor_brick (int dispensor_id, int  brick_id) {
    if (brick_id == -1) fx_dispensors[dispensor_id].pos = -1;
    fx_dispensors[dispensor_id].brick_id = brick_id;
    rebuild_positions();
}

void set_dispensor_pos   (int dispensor_id, int  pos) {
    fx_dispensors[dispensor_id].pos = pos;
    rebuild_positions();
}

void set_dispensor_status(int dispensor_id, int status) {
    if (dispensor_id < 0 || dispensor_id >= MAX_NUMBER_DISPENSORS) return;
    fx_dispensors[dispensor_id].status = status;
}

// --- Position Utils ---
bool is_legal_placement (int pos, int width) {
    if (pos < 0 || pos + width > 28) return false;
    for (size_t i = 0; i < width; i++) {
        if (positions[i + pos]) return false;
    }
    return true;
}
