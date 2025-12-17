<p align=center> <img src=resources/mklego-icon128.png> </p>

# CS-358 Project : MKLEGO 

 <p align=center><img src=resources/readme-screenshots/Full_printer.png width=450> </p>

## Project overview

MKLEGO is a 3D printer that uses LEGO bricks to build any given structure layer by layer. The project is based on mofifying a PRUSA MK3 3D printer by 
- replacing the extruder with a custom nozzle that can pick, place and rotate LEGO bricks,
- changing the base plate to a LEGO compatible plate with place for custom LEGO brick dispensers,
- developing custom software to convert 3D models into LEGO brick instructions.


## Software
### How to run
Access the software repository here: [MKLEGO Software](/frontend/).
Run it by :
- From the `frontend` folder, run the project with 
   
   `./gradlew :desktop-app:run`.
- The application window should open. You can see your recent projects and open them if there are any.

Creating a new project :
- To create a new project, click on File > New File, then select an STL or lxfml file to load and slice a 3D model, or nothing to create a blank project.
- You can go back to the recent projects by clicking on File > Recent Projects.

Editing :
- Once you have opened a project, you can add, remove, or paint LEGO bricks.
- You can see the controls by pressing `H`.
- To paint an existing brick: select the targeted brick, then click on a color in the palette.

Supports :
- The translucid bricks are support bricks. They are added automatically underneath hanging bricks. You can also add some manually by using the gray color (rightmost) in the color palette. You can hide/show them by clicking on "Hide Supports" in the top right.

Printing :
- To export your LEGO project: open the desired project and go to File > Export as lgcode.
- To print your LEGO structure, export it as lgcode, store it in a SD card and transfer the file to the printer. Then, you can set the printer and launch the build through the printer's screen interface.


### Pipeline
The JavaFX-based software pipeline includes:
- Loading a 3D model from an STL or lxfml (v5.0 Legacy LDD compatible) file,
- Slicing it into LEGO brick layers and assembling them in an optimal agencement,
- Rendering the LEGO structure in a 3D viewer to view and edit the build if needed,
- Generating the lgcode (custom g-code) instructions to be sent to the printer.


## Hardware architecture
 <p align=center><img src=resources/readme-screenshots/schematic-overview.png> </p>
 More details in the [Hardware Documentation](/resources/Mklego_Diagram.pdf).

### Firmware
Access the firmware repository here: [MKLEGO Firmware](/embedded/mklego).

### System Overview

The hardware is organized around a **dual-microcontroller architecture**:

- **Main Controller**: STM32 Nucleo-64 (NUCLEO-F411RE)  
  Responsible for motion control (stepper motors), safety-critical timing, and real-time coordination.

- **Baseplate Controller**: ESP32-S3-DevKitC  
  Responsible for high-level logic, sensor acquisition, dispenser module control, and communication with the main controller.

The system integrates:
- Up to **6 LEGO dispenser modules**
- **3-axis stepper motor control (X, Y, Z)**
- A **servo motor** that pushes the brick down through the nozzle
- A centralized **power supply unit (PSU)** providing 5 V and 12 V rails


# Building Tutorial

This section explains how to physically build the MKLEGO prototype from a Prusa MK3 printer and the provided custom parts.  
All mechanical parts (DXF, STL and 3MF files) are available in the repository.

---

## Step 1: Preparation

### Required base machine
This prototype **requires a Prusa MK3 printer** as the mechanical base (frame, linear rails, motors).

### Disassembly
Before starting the build:

1. Power off and unplug the printer.
2. Remove the following components from the Prusa MK3:
   - Heated bed
   - Original extruder and hotend
   - Power Supply Unit (PSU)

⚠️ These components are **not used** in the MKLEGO system.

At the end of this step, you should keep:
- The printer frame
- X/Y/Z motion system
- Stepper motors

---

## Step 2: Mechanical Assembly Overview

The MKLEGO hardware is composed of three main subsystems:

1. **Custom LEGO Nozzle**
2. **LEGO-Compatible Base Plate**
3. **LEGO Brick Dispensers**

Each subsystem can be assembled independently before final integration on the printer.

---

## Nozzle Assembly

<p align="center">
  <img src="resources/readme-screenshots/Nozzle.png" width="450">
</p>

---

### Materials

- [ ] 1 × NEMA 14 stepper motor  
- [ ] 1 × Servo motor (15 kg·cm recommended)  
- [ ] 2 × Brass hollow cylinders  
  - Outer diameter: 8 mm  
  - Inner diameter: 5 mm  
  - Length: 64 mm  
- [ ] 2 × Metal rods (97 mm long)  
- [ ] 3 × Springs (10 mm length)  
- [ ] 1 × Micro-switch  
- [ ] M3 and M2.5 screws and nuts  
- [ ] MDF parts (laser cut)  
- [ ] PETG parts (3D printed)

---

### Assembly Process

1. **Prepare the parts**
   - Laser cut all MDF components using the provided DXF files.
   - 3D print all plastic parts (PETG).

2. **Linear structure**
   - Assemble all sliding components along the two metal rods.
   - Ensure smooth motion without excessive friction.

3. **Rotation mechanism**
   - Mount the servo motor.
   - Assemble the rotating LEGO-gripping section.

4. **Final enclosure**
   - Add the fixed (non-rotating) outer structure.
   - Install the micro-switch for rotation calibration.

A fully assembled 3D model of the nozzle is available as a **3MF file** for reference.

---

## Base Plate Assembly

<p align="center">
  <img src="resources/readme-screenshots/BasePlate.png" width="450">
</p>

---

### Materials

- [ ] 1 × Micro-switch  
- [ ] MDF parts (laser cut)  
- [ ] PETG parts (3D printed)  
- [ ] 1 × LEGO base plate (22 × 22 studs)

---

### Assembly Process

1. Fabricate all MDF and PETG parts using the provided DXF and STL files.
2. Assemble the mechanical structure of the base plate.
3. Install the micro-switch for Y-height referencing.
4. **You do not need mount the LEGO plate yet**
   
A complete assembled model is also available as a **3MF file**.

---

## Dispenser Assembly

<p align="center">
  <img src="resources/readme-screenshots/Dispensers.png" width="450">
</p>

---

### Materials (per dispenser)

- [ ] 1 × IR LED  
- [ ] 1 × IR Receiver  
- [ ] M3 screws and nuts  
- [ ] MDF parts (laser cut)  
- [ ] PETG parts (3D printed)

---

### Assembly Process

1. Assemble the mechanical ramp structure.
2. Install the IR LED and IR receiver:
   - Align them carefully to ensure reliable brick detection.
3. Test brick feeding manually before electronic integration.

A complete dispenser assembly is provided as a **3MF file**.

---

## Next Steps

After completing the mechanical assembly:


