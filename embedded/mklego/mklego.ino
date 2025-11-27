
#include "lgcode.h"
#include "physics.h"

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);

  reset_lgcode();
}

void show_config () {
  Serial.println("=== COLORS ===");
  for (int idx = 0; idx < get_number_color(); idx ++) {
    uint8_t color_id = get_colors_uuids()[ idx ];

    Serial.print("Color #");
    Serial.print((int) color_id);
    Serial.print(" ");

    brick_color color = *get_color(color_id);
    Serial.print("(");
    Serial.print((const char*) color.name);
    Serial.print("): rgba(");
    Serial.print((int) color.red);
    Serial.print(", ");
    Serial.print((int) color.green);
    Serial.print(", ");
    Serial.print((int) color.blue);
    Serial.print(", ");
    Serial.print((int) color.alpha);
    Serial.println(")");
  }

  Serial.println("=== BRICKS ===");
  for (int idx = 0; idx < get_number_types(); idx ++) {
    uint8_t type_id = get_types_uuids()[ idx ];
    
    Serial.print("Brick #");
    Serial.print((int) type_id);
    Serial.print(": ");

    brick_type type = *get_type(type_id);

    Serial.print(type.size_x);
    Serial.print("x");
    Serial.print(type.size_y);
    Serial.print(" ");
    
    brick_color color = *get_color(type.color);
    Serial.print(color.name);
    Serial.print(" R=");
    Serial.println(type.resistor);
  }
  Serial.println("=== PLATE ===");
  if (plate_size_configured()) {
    Serial.print("size_x = ");
    Serial.println(get_plate_size_x());
    Serial.print("size_y = ");
    Serial.println(get_plate_size_y());
  } else {
    Serial.println("Plate not configured");
  }
  Serial.println();
}

unsigned char sample_lgcode[] = {
   66,  45,  76,  71, 67,  79,  68,  69,
    0,   0,   3,   8,  8,   1,   1, 255, 
    0,   0, 255,   0,  0,   0,   3,  82,
   69,  68,   0,   0,  0,  12,  65,  32,
  114, 101, 100,  32, 99, 111, 108, 111,
  114,  46,   2,   1,  0,   0,   0,   7,
  115, 116, 100,  95, 52,  95,  50,   1,
    0,   0,   0, 100,  0,   1,   4,   1,
    1,   6,   1,   7,  3,   1,   0,   5, 
    6,   0,   4,   1,  0,   6,   1,   7,
    3,   4,   0,   5
};

bool shown_config = false;
int sent = 0;
bool run_placement = false;
void loop() {
  if (sent != sizeof(sample_lgcode)) {
    int rem = sizeof(sample_lgcode) - sent;
    sent += write_lgcode(sample_lgcode + sent, 32 < rem ? 32 : rem);
  }

  if (!run_placement) {
    run_placement = true;
    initPhysics();
    calibrateAll();

    dispensorMoveReferential().moveTo(0, 0, 0);
    nozzleUp();
    dispensorDownReferential().moveTo(0, 0, 0);
    dispensorMoveReferential().moveTo(0, 0, 0);
    plateMoveReferential().moveTo(10, 10, 0);
    plateDownReferential().moveTo(10, 10, 0);
    nozzleDown();
    plateMoveReferential().moveTo(10, 10, 0);
  }

  if (had_error()) {
    Serial.println("Error when parsing");
    Serial.print(" - ");
    Serial.println(error_text());
    delay(10000);
  } else if (in_print_section()) {
    if (!shown_config) {
      shown_config = true;
      show_config();
    } else {
      while (has_current_operation()) {
        switch (current_operation_type()) {
          case GRAB:
            Serial.print("GRAB(");
            Serial.print(get_grab_operation().brick_id);
            Serial.print(", ");
            Serial.print(get_grab_operation().attachment_id);
            Serial.println(")");
            break ;
          case MOVE:
            Serial.print("MOVE(");
            Serial.print(get_move_operation().x);
            Serial.print(", ");
            Serial.print(get_move_operation().y);
            Serial.print(", ");
            Serial.print(get_move_operation().z);
            Serial.println(")");
            break ;
          case ROTATE:
            Serial.print("ROTATE(");
            Serial.print(get_rotate_operation().rotation);
            Serial.println(")");
            break ;
          case DROP:
            Serial.println("DROP");
            break ;
        }

        pop_current_operation();
      }
    }
  }
}
