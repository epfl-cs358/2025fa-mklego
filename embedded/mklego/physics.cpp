
#include "physics.h"

#include <AccelStepper.h>
#include <MultiStepper.h>
#include <Servo.h>

static AccelStepper stepperX(AccelStepper::DRIVER, X_STEP_PIN, X_DIR_PIN);
static AccelStepper stepperY(AccelStepper::DRIVER, Y_STEP_PIN, Y_DIR_PIN);
static AccelStepper stepperZ(AccelStepper::DRIVER, Z_STEP_PIN, Z_DIR_PIN);
static AccelStepper stepperR(AccelStepper::DRIVER, R_STEP_PIN, R_DIR_PIN);

static MultiStepper multi;

static Servo nozzleServo;

void enableMotors () {
  digitalWrite(ENABLE_PIN, LOW);
}
void disableMotors () {
  if (CAN_DISABLE_MOTORS) {
    digitalWrite(ENABLE_PIN, HIGH);
  }
}

void initPhysics () {
  pinMode(X_MICRO, INPUT_PULLUP);
  pinMode(Y_MICRO, INPUT_PULLUP);
  pinMode(Z_MICRO, INPUT_PULLUP);
  pinMode(R_MICRO, INPUT_PULLUP);

  pinMode(ENABLE_PIN, OUTPUT);
  enableMotors ();
  disableMotors();

  nozzleServo.attach(SERVO_PIN);
  nozzleDown ();

  stepperX.setMaxSpeed(MOVE_SPEED);
  stepperY.setMaxSpeed(MOVE_SPEED);
  stepperZ.setMaxSpeed(MOVE_SPEED);
  stepperR.setMaxSpeed(ROTATION_SPEED);
  
  multi.addStepper(stepperX);
  multi.addStepper(stepperY);
  multi.addStepper(stepperZ);
}

void calibrate (int pin, int sign, AccelStepper &stepper, float start_speed, float end_speed) {
  enableMotors();

  stepper.setCurrentPosition(0);
  stepper.moveTo(100000LL);
  stepper.setSpeed(start_speed * sign);
  while (digitalRead(pin) == LOW) {
    stepper.runSpeed();
  }
  stepper.setCurrentPosition(0);
  
  stepper.moveTo(100000LL);
  stepper.setSpeed(- end_speed * sign);
  while (digitalRead(pin) == HIGH) {
    stepper.runSpeed();
  }
  stepper.setCurrentPosition(0);
  
  disableMotors();
}





void calibrateX () {
  calibrate(X_MICRO, -1, stepperX, CALIBRATION_XYZ_SPEED1, CALIBRATION_XYZ_SPEED2);
}
void calibrateY () {
  calibrate(Y_MICRO, -1, stepperY, CALIBRATION_XYZ_SPEED1, CALIBRATION_XYZ_SPEED2);
}
void calibrateZ () {
  calibrate(Z_MICRO, 1, stepperZ, CALIBRATION_XYZ_SPEED1, CALIBRATION_XYZ_SPEED2);
}
void calibrateR () {
  calibrate(R_MICRO, 1, stepperR, CALIBRATION_R_SPEED1, CALIBRATION_R_SPEED2);
  
  stepperR.move(R_DELTA);
  stepperR.setSpeed(ROTATION_SPEED);
  while (stepperR.distanceToGo() != 0) {
    stepperR.runSpeedToPosition();
  }

  stepperR.setCurrentPosition(0);
}
void calibrateAll () {
  calibrateZ();
  calibrateX();
  calibrateR();
  calibrateX();
  calibrateY();
}



void nozzleUp () {
  nozzleServo.write(NOZZLE_UP);
  delay(NOZZLE_DELAY);
}
void nozzleDown () {
  nozzleServo.write(NOZZLE_DOWN);
  delay(NOZZLE_DELAY);
}
void moveMotorsTo (long x, long y, long z) {
  long positions[3];
  positions[0] = x;
  positions[1] = y;
  positions[2] = z;

  Serial.print("MOVE TO ");
  Serial.print(x);
  Serial.print(" ");
  Serial.print(y);
  Serial.print(" ");
  Serial.println(z);

  enableMotors ();
  multi.moveTo(positions);
  multi.runSpeedToPosition();
  disableMotors ();
}
void rotateRight() {
  stepperR.move(ROTATION_90);
  stepperR.setSpeed(ROTATION_SPEED);
  while (stepperR.distanceToGo() != 0) {
    stepperR.runSpeedToPosition();
  }
}

void rotateLeft() {
  stepperR.move(-ROTATION_90);
  stepperR.setSpeed(ROTATION_SPEED);
  while (stepperR.distanceToGo() != 0) {
    stepperR.runSpeedToPosition();
  }
}


Referential::Referential (
  long originx, long originy, long originz,
    
  long minx, long miny, long minz,
  long maxx, long maxy, long maxz,
  long scax, long scay, long scaz
) {
  this->originx = originx;
  this->originy = originy;
  this->originz = originz;

  this->minx = minx; this->miny = miny; this->minz = minz;
  this->maxx = maxx; this->maxy = maxy; this->maxz = maxz;
  this->scax = scax; this->scay = scay; this->scaz = scaz;
}

bool Referential::moveTo (long x, long y, long z) {
  if (x < minx || y < miny || z < minz) return false;
  if (x > maxx || y > maxy || z > maxz) return false;
  
  moveMotorsTo(
    originx + scax * x, 
    originy + scay * y, 
    originz + scaz * z);

  return true;
}

bool Referential::wiggle (long x, long y, long z) {
  if (x < minx || y < miny || z < minz) return false;
  if (x > maxx || y > maxy || z > maxz) return false;

  long stepX = originx + scax * x;
  long stepY = originy + scay * y;
  long stepZ = originz + scaz * z;

  long vx;
  long vy;

  stepperX.setMaxSpeed(WIGGLE_SPEED);
  stepperY.setMaxSpeed(WIGGLE_SPEED);
  for (float t = 0.0; t < 2*PI; t+= PI / 8.) {
    vx = stepX + WIGGLE_RADIUS * cos(t);
    vy = stepY + WIGGLE_RADIUS * sin(t); 
    moveMotorsTo(vx, vy, stepZ);
    moveMotorsTo(stepX, stepY, stepZ);
  }
  moveMotorsTo(stepX, stepY, stepZ);
  stepperX.setMaxSpeed(MOVE_SPEED);
  stepperY.setMaxSpeed(MOVE_SPEED);
  return true;
}

static Referential _dispensorDownReferential = Referential(1400, 180, -74500, 0, 0, 0, 25, 0, 0, XY_LEGO_WIDTH, 0, 0);
static Referential _dispensorMoveReferential = Referential(1400, 180, -65000, 0, 0, 0, 25, 0, 17, XY_LEGO_WIDTH, 0, 3750);
static Referential _plateDownReferential = Referential(4250, 4080, -74500, 0, 0, 0, 18, 18, 17, XY_LEGO_WIDTH, XY_LEGO_WIDTH, 3750);
static Referential _plateWiggleReferential = Referential(4250, 4080, -71250, 0, 0, 0, 18, 18, 17, XY_LEGO_WIDTH, XY_LEGO_WIDTH, 3750);
static Referential _plateMoveReferential = Referential(4250, 4080, -65000, 0, 0, 0, 18, 18, 17, XY_LEGO_WIDTH, XY_LEGO_WIDTH, 3750);


Referential& dispensorDownReferential () { return _dispensorDownReferential; }
Referential& dispensorMoveReferential () { return _dispensorMoveReferential; }
Referential& plateDownReferential () { return _plateDownReferential; }
Referential& plateWiggleReferential () { return _plateWiggleReferential; }
Referential& plateMoveReferential () { return _plateMoveReferential; }
