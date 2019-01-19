/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import com.ctre.phoenix.motorcontrol.can.*;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.CameraServer;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.*;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;



/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory
 */
public class Robot extends IterativeRobot {
    //Motor Controllers - Drive
  /* talons for arcade drive */
  WPI_TalonSRX _frontLeftMotor = new WPI_TalonSRX(1);
  WPI_TalonSRX _frontRightMotor = new WPI_TalonSRX(3);
  
  /* extra talons and victors for six motor drives */
  WPI_TalonSRX _leftSlave1 = new WPI_TalonSRX(2);
  WPI_VictorSPX _rightSlave1 = new WPI_VictorSPX(4);
  
  
  DifferentialDrive _drive = new DifferentialDrive(_frontLeftMotor, _frontRightMotor);
  
  Joystick _joy = new Joystick(0);
  XboxController driver = new XboxController(0);
  int frames = 30;
  double currentData;

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    @Override
    public void robotInit() {
       new Thread(() -> {
        
        UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
        
        camera.setVideoMode(VideoMode.PixelFormat.kMJPEG, 360, 320, frames);

        CvSink cvSink = CameraServer.getInstance().getVideo();
        CvSource outputStream = CameraServer.getInstance().putVideo("Blur", 480, 320);

        Mat source = new Mat();
        Mat output = new Mat();
        
        while(!Thread.interrupted()) {
            cvSink.grabFrame(source);
            currentData = camera.getActualDataRate();
            if (currentData > 3.5 && frames > 8){
              frames = frames - 2;
              camera.setVideoMode(VideoMode.PixelFormat.kMJPEG, 360, 320, frames);
            }else if (currentData < 1.5 && frames < 30){
              frames = frames + 2;
              camera.setVideoMode(VideoMode.PixelFormat.kMJPEG, 360, 320, frames);
            }
            Imgproc.cvtColor(source, output, Imgproc.COLOR_BGR2GRAY);
            outputStream.putFrame(output);
        }
    }).start(); 
    
      /*
      * take our extra talons and just have them follow the Talons updated in
      * arcadeDrive
      */
  
     _leftSlave1.follow(_frontLeftMotor);
     _rightSlave1.follow(_frontRightMotor);
  
     /* drive robot forward and make sure all 
      * motors spin the correct way.
      * Toggle booleans accordingly.... */
     _frontLeftMotor.setInverted(false);
     _leftSlave1.setInverted(false);
     
     _frontRightMotor.setInverted(true);
     _rightSlave1.setInverted(true);
   }
    /**
     * This function is run once each time the robot enters autonomous mode.
     */
    @Override
    public void autonomousInit() {
      
    }
  
    /*
     * This function is called periodically during autonomous.
     */
    @Override
    public void autonomousPeriodic() {
  
    }
  
    /**
     * This function is called once each time the robot enters teleoperated mode.
     */
    @Override
    public void teleopInit() {
      
    }
  
    /**
     * This function is called periodically during teleoperated mode.
     */
    @Override
    public void teleopPeriodic() {
      double forward = -1.0 * _joy.getY();
      /* sign this so right is positive. */
      double turn = +1.0 * _joy.getX();
      /* deadband */
      if (Math.abs(forward) < 0.4) {
        /* within 10% joystick, make it zero */
        forward = 0;
      }
      if (Math.abs(turn) < 0.4) {
        /* within 10% joystick, make it zero */
        turn = 0;
      }
      /* print the joystick values to sign them, comment
       * out this line after checking the joystick directions. */
      System.out.println("JoyY:" + forward + "  turn:" + turn );
      /* drive the robot, when driving forward one side will be red.  
       * This is because DifferentialDrive assumes 
       * one side must be negative */
      _drive.arcadeDrive(forward, turn);
    }
  
  
    /**
     * This function is called periodically during test mode.
     */
    @Override
    public void testPeriodic() {
      
    }
  }