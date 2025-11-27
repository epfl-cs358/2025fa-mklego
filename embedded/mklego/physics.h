
// ############################
// ######## CONSTANTS #########
// ############################

#define CAN_DISABLE_MOTORS true

#define X_STEP_PIN 3
#define X_DIR_PIN 2
#define Y_STEP_PIN 5
#define Y_DIR_PIN 4
#define Z_STEP_PIN 7
#define Z_DIR_PIN 6
#define R_STEP_PIN 9
#define R_DIR_PIN 8

#define X_MICRO 38
#define Y_MICRO 42
#define Z_MICRO 40
#define R_MICRO 36

#define XY_LEGO_WIDTH 800

#define ENABLE_PIN 32
#define SERVO_PIN 30

#define MOVE_SPEED 3000
#define ROTATION_SPEED 100

#define CALIBRATION_XYZ_SPEED1 3000.0
#define CALIBRATION_XYZ_SPEED2 200
#define CALIBRATION_R_SPEED1 100.0
#define CALIBRATION_R_SPEED2 50

#define R_DELTA -49
#define ROTATION_90 50

#define NOZZLE_UP 0
#define NOZZLE_DOWN 120

void initPhysics ();

// #############################
// ######## CALIBRATION ########
// #############################

void calibrateX ();
void calibrateY ();
void calibrateZ ();
void calibrateR ();

void calibrateAll ();

// ############################
// ########## NOZZLE ##########
// ############################

void nozzleUp   ();
void nozzleDown ();

void rotateRight ();
void rotateLeft ();

// ############################
// ######### MOVEMENT #########
// ############################

struct Referential {
private:
  int originx, originy, originz;
  
  int minx, miny, minz;
  int maxx, maxy, maxz;
  int scax, scay, scaz;
public:
  Referential (
    int originx, int originy, int originz,
    
    int minx, int miny, int minz,
    int maxx, int maxy, int maxz,
    int scax, int scay, int scaz
  );

  bool moveTo (int x, int y, int z);
};

Referential dispensorDownReferential = Referential(1500, 280, -74500, 0, 0, 0, 25, 0, 0, XY_LEGO_WIDTH, 0, 0);
Referential dispensorMoveReferential = Referential(1500, 280, -65000, 0, 0, 0, 25, 0, 0, XY_LEGO_WIDTH, 0, 0);
Referential plateDownReferential = Referential(4350, 4200, -74500, 0, 0, 0, 18, 18, 0, XY_LEGO_WIDTH, -XY_LEGO_WIDTH, 0);
Referential placeMoveReferential = Referential(4350, 4200, -65000, 0, 0, 0, 18, 18, 0, XY_LEGO_WIDTH, -XY_LEGO_WIDTH, 0);
