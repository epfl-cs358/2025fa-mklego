
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
   66, 45, 76, 71, 67, 79, 68, 69, 0, 0, 3, 20, 20, 1, 0, 255, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 7, 115, 116, 100, 95, 52, 95, 50, 0, 0, 0, 0, 1, 2, 1, 0, 0, 0, 7, 115, 116, 100, 95, 50, 95, 50, 0, 0, 0, 0, 2, 0, 1, 6, 1, 4, 0, 0, 6, 0, 7, 3, 4, 0, 5, 6, 1, 4, 0, 0, 7, 5, 4, 0, 5, 6, 1, 4, 0, 0, 7, 11, 4, 0, 5, 6, 1, 4, 0, 0, 6, 0, 7, 15, 4, 0, 5, 6, 1, 4, 0, 0, 6, 0, 7, 3, 8, 0, 5, 6, 1, 4, 0, 0, 6, 0, 7, 15, 8, 0, 5, 6, 1, 4, 0, 0, 6, 0, 7, 3, 12, 0, 5, 6, 1, 4, 0, 0, 6, 0, 7, 15, 12, 0, 5, 6, 1, 4, 0, 0, 7, 5, 14, 0, 5, 6, 1, 4, 1, 0, 7, 9, 14, 0, 5, 6, 1, 4, 0, 0, 7, 11, 14, 0, 5, 6, 1, 4, 0, 0, 7, 3, 4, 1, 5, 6, 1, 4, 1, 0, 7, 7, 4, 1, 5, 6, 1, 4, 1, 0, 7, 11, 4, 1, 5, 6, 1, 4, 0, 0, 7, 13, 4, 1, 5, 6, 1, 4, 0, 0, 6, 0, 7, 3, 6, 1, 5, 6, 1, 4, 0, 0, 6, 0, 7, 15, 6, 1, 5, 6, 1, 4, 0, 0, 6, 0, 7, 3, 10, 1, 5, 6, 1, 4, 0, 0, 6, 0, 7, 15, 10, 1, 5, 6, 1, 4, 0, 0, 7, 3, 14, 1, 5, 6, 1, 4, 0, 0, 7, 7, 14, 1, 5, 6, 1, 4, 1, 0, 7, 11, 14, 1, 5, 6, 1, 4, 0, 0, 7, 13, 14, 1, 5, 6, 1, 4, 0, 0, 6, 0, 7, 3, 4, 2, 5, 6, 1, 4, 1, 0, 7, 5, 4, 2, 5, 6, 1, 4, 0, 0, 7, 7, 4, 2, 5, 6, 1, 4, 0, 0, 7, 11, 4, 2, 5, 6, 1, 4, 0, 0, 6, 0, 7, 15, 4, 2, 5, 6, 1, 4, 0, 0, 6, 0, 7, 3, 8, 2, 5, 6, 1, 4, 0, 0, 6, 0, 7, 15, 8, 2, 5, 6, 1, 4, 0, 0, 6, 0, 7, 3, 12, 2, 5, 6, 1, 4, 0, 0, 6, 0, 7, 15, 12, 2, 5, 6, 1, 4, 0, 0, 7, 5, 14, 2, 5, 6, 1, 4, 0, 0, 7, 9, 14, 2, 5, 6, 1, 4, 1, 0, 7, 13, 14, 2, 5, 6, 1, 4, 1, 0, 7, 3, 4, 3, 5, 6, 1, 4, 1, 0, 7, 7, 4, 3, 5, 6, 1, 4, 1, 0, 7, 11, 4, 3, 5, 6, 1, 4, 1, 0, 7, 15, 4, 3, 5, 6, 1, 4, 1, 0, 7, 3, 9, 3, 5, 6, 1, 4, 1, 0, 7, 15, 9, 3, 5, 6, 1, 4, 1, 0, 7, 3, 14, 3, 5, 6, 1, 4, 1, 0, 7, 7, 14, 3, 5, 6, 1, 4, 1, 0, 7, 11, 14, 3, 5, 6, 1, 4, 1, 0, 7, 15, 14, 3, 5
};

void printAction(String title, long x, long y, long z) {
  Serial.print(title);
  Serial.print(" : ");
  Serial.print(x);
  Serial.print(", ");
  Serial.print(y);
  Serial.print(", ");
  Serial.println(z);
}

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
      long x;
      long y;
      long z = 0;
      long dispX;
      long r;
      while (has_current_operation()) {
        Serial.print("Current op: ");
        Serial.println(current_operation_type());

        switch (current_operation_type()) {
          case GRAB:
            {
              Serial.print("GRAB(");
              Serial.print(get_grab_operation().brick_id);
              Serial.print(", ");
              Serial.print(get_grab_operation().attachment_id);
              Serial.println(")");

              const brick_type* brick = get_type(get_grab_operation().brick_id);
              dispX = get_grab_operation().attachment_id;
              if (brick->size_x == 2) {
                dispX += 5;
              }

              dispensorMoveReferential().moveTo(dispX, 0, max(z, 2));
              nozzleUp();
              dispensorDownReferential().moveTo(dispX, 0, 0);
              dispensorMoveReferential().moveTo(dispX, 0, max(z, 2));

              break;
            }
            
          case MOVE:
            {
              x = get_move_operation().x;
              y = get_move_operation().y;
              z = get_move_operation().z;

              Serial.print("MOVE(");
              Serial.print(x);
              Serial.print(", ");
              Serial.print(y);
              Serial.print(", ");
              Serial.print(z);
              Serial.println(")");

              dispensorMoveReferential().moveTo(dispX, 0, max(z, 2));
              plateMoveReferential().moveTo(x, y, max(z, 2));
              plateWiggleReferential().moveTo(x, y, z);
              plateWiggleReferential().wiggle(x, y, z);
              plateDownReferential().moveTo(x, y, z);
              plateDownReferential().wiggle(x, y, z);

              break ;
            }
            
          case ROTATE:
            {
              r = get_rotate_operation().rotation;

              Serial.print("ROTATE(");
              Serial.print(r);
              Serial.println(")");

              rotateNozzle(r);
                  
              break ;
            }
            
          case DROP:
            {
              Serial.println("DROP");

              nozzleDown();
              plateMoveReferential().moveTo(x, y, max(z, 2));

              break ;
            }
        }
        pop_current_operation();
      }
    }
  }
}
