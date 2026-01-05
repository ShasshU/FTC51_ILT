
# FTC Team 51 Auto Turret Engineering Documentation

## 1. Overview
The turret subsystem automatically aims the shooter toward the alliance high goal.  
It uses robot position, heading, and turret offset to compute the angle to the goal.  
This document explains the geometry, angle calculation, encoder mapping, and control logic.

## 2. FTC Field Geometry
- **Field size:** `144 in × 144 in`
- **Goals:**  
  - Blue goal: `(0, 144)`  
  - Red goal: `(144, 144)`
- **Robot pose:** `(x_r, y_r)` in inches; heading `θ_r` in radians
- **Turret offset:** `(Δx_t, Δy_t) = (−6.65, 0)` inches (behind robot reference)
- **Turret position:**  
  ```
  x_t = x_r + Δx_t
  y_t = y_r + Δy_t
  ```

## 3. Diagram
*(Visual representation of robot, turret offset, goal, and angle arc)*  

## 4. Auto Turret Aim at Target Calculation
- **Conventional bearing:**  
  ```
  α = atan2(y_g − y_t, x_g − x_t)
  α° = α × (180 / π)
  ```
- **Explicit mount offset:**  
  ```
  θ_mount (e.g., −90° if encoder zero points left)
  ```
- **Turret command angle:**  
  ```
  θ_cmd = α° + θ_r° − 90°
  ```
- Normalize angle to `[-180°, +180°]`
- Clamp to mechanical limits:  
  ```
  θ_cmd ∈ [−90°, +90°]
  ```
- **Tolerance in degrees:**  
  ```
  |θ_target − θ_current| < tol_deg
  ```
- **Gear mapping:**  
  ```
  T_rot = CPR × MOTOR_TO_TURRET_RATIO × EMPIRICAL_CORRECTION
  ```
- **Conversions:**  
  ```
  ticks = round((θ_cmd / 360°) × T_rot)
  θ = (ticks / T_rot) × 360°
  ```
- **Controller:**  
  ```
  Power = clamp(kP × (ticks_target − ticks_current), −1, +1)
  ```
- Optional: Add PD or motion profiling for smoother response.


## 5. Quick Formula Summary
```
Turret position: x_t = x_r + Δx_t, y_t = y_r + Δy_t
Goals: (x_g, y_g) = (0,144) Blue or (144,144) Red
bearing: α = atan2(y_g − y_t, x_g − x_t)
Turret command: θ_cmd = normalize(α° − θ_r° + θ_mount)
Clamp: θ_cmd ∈ [−90°, +90°]
Ticks per rotation: T_rot = CPR × ratio × correction
Angle → ticks: ticks = round((θ_cmd / 360°) × T_rot)
Ticks → angle: θ = (ticks / T_rot) × 360°
Control: Power = clamp(kP × (ticks_target − ticks_current), −1, +1)
```
---
# Auto Turret – 

## What problem are we solving?
During a match, the robot must aim its shooter accurately at the high goal while the robot is moving.  
Manually aiming the turret would slow the driver and reduce accuracy.  
Our solution is an **automatic turret** that continuously aims at the goal using the robot’s position and heading.


## How does the turret know where to aim?
Our robot always knows:
- Where it is on the field (X, Y position)
- Which direction it is facing (heading)
- Where the goal is located on the field

Using this information, the turret calculates the direction from the robot to the goal and rotates to that angle automatically.


## Field and Robot Understanding
- The FTC field is **144 inches × 144 inches**
- The goal location is fixed and known
- The robot’s position and heading come from **odometry (Pinpoint / Pedro Pathing)**
- The turret is not mounted at the exact center of the robot, so we account for its physical offset

This ensures the turret aims from its **actual position**, not just the robot center.


## How the angle is calculated (conceptual)
1. We calculate the direction from the turret to the goal on the field.
2. We adjust that direction based on how the robot is rotated.
3. We apply a fixed correction for how the turret is mounted on the robot.
4. The result is the angle the turret must rotate to aim at the goal.

All angles are kept within safe mechanical limits so the turret never over-rotates.


## Coordinated system 
uses a coordinate system where:
- Forward is the positive Y direction
- Robot heading increases clockwise

Because of this, some angles are added instead of subtracted.  
This is intentional and documented to match FTC sensors and odometry behavior.


## How the turret physically moves
- The turret uses a motor with an encoder
- We convert the desired angle into encoder ticks
- A proportional controller moves the motor until the turret reaches the target angle
- The turret holds its position once aligned

This provides smooth and repeatable aiming.


## Reliability and Safety
- Angle commands are clamped to mechanical limits
- A tolerance band prevents jitter at the target
- The system continues aiming even while the robot moves


## Design Flexibility
Our system is designed so it can be upgraded to:
- Full 360° continuous rotation
- Shortest-path rotation logic
- More advanced control (PD or motion profiling)

This makes the turret future-proof and adaptable.


## Why this matters in a match
- Faster scoring
- Reduced driver workload
- More consistent accuracy
- Better performance while moving

The auto turret allows the driver to focus on positioning while the robot handles aiming automatically.

---
# Auto Turret – Design Evolution Story

## 1. Early Season Approach: No Turret
At the beginning of the season, our robot did not include a turret.  
Our strategy relied on driving the robot to precise positions and orientations on the field and shooting from fixed angles.

To support this, we focused heavily on **accurate odometry and path following** using **Pedro Pathing**.  
By carefully tuning paths and stopping at known locations, we were able to aim the shooter by rotating the entire robot.

This approach worked under ideal conditions and helped us build a strong foundation in autonomous navigation.


## 2. At Meet 2 Robot: Odometry-Driven Accuracy
By Meet 2, we had improved our robot’s consistency using:
- Pinpoint odometry for accurate position tracking
- Pedro Pathing for smooth and repeatable robot motion

The robot could reach scoring positions reliably, but accurate shooting still required:
- Precise stopping
- Exact robot orientation
- Extra driver effort to line up shots

Even small errors in heading or position caused missed shots, especially when shooting while under time pressure.


## 3. Identified Limitation
Through match testing, we observed that:
- Rotating the entire robot to aim slowed down scoring
- Small heading errors had a large impact on shot accuracy
- Driver workload increased significantly during fast-paced cycles

We concluded that **navigation accuracy alone was not enough**.  
The robot needed a way to aim independently of its driving direction.


## 4. Decision to Add a Turret
To solve this problem, we introduced an **auto-aiming turret**.

The turret allows:
- Independent aiming while the robot continues moving
- Faster shot alignment
- Reduced dependency on perfect robot orientation

This was a major design change, but it directly addressed the limitations we saw during Meet 2.


## 5.Integrating Odometry with the Turret
Rather than replacing our existing systems, we built the turret to **reuse the same odometry data**:
- Robot position and heading from Pinpoint
- Path execution from Pedro Pathing

We extended our geometry model to account for:
- The turret’s physical offset from the robot center
- The fixed location of the goal on the field

This allowed the turret to aim accurately from anywhere on the field.


## 6.Control and Refinement
Initial turret control used open-loop motor commands, which caused overshoot.

We improved performance by:
- Adding encoder feedback
- Converting target angles to encoder ticks
- Implementing proportional control for smooth movement

We also added mechanical and software limits to protect wiring and ensure reliability.


## 7.Final Outcome
The final system combines:
- Accurate odometry and path planning (Pedro Pathing)
- Automatic turret aiming
- Safe and repeatable motor control

This design allows the robot to score faster, with less driver effort, and with higher consistency than our early season robot.

## 8.Lesson Learned
Our biggest lesson was that **precision driving alone is not sufficient for fast and consistent scoring**.  
Separating driving and aiming responsibilities through a turret significantly improved performance.

This evolution demonstrates how match observations and testing directly informed our engineering decisions.
