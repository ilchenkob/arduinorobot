#include <SoftwareSerial.h>

const int DIRECTION_CHANGE_DELAY = 200;

const int LEFT = 1;
const int RIGHT = 2;

const int _FORWARD = 9;
const int _BACKWARD = 7;

const int ML1A = 2; // мотор левый №1
const int ML1B = 4; // мотор левый №1
const int ML1C = 3; // ШИМ

const int ML2A = 6; // мотор левый №2
const int ML2B = 7; // мотор левый №2
const int ML2C = 5; // ШИМ

const int MR1A = 8; // мотор правый №1
const int MR1B = 9; // мотор правый №1
const int MR1C = 10; // ШИМ

const int MR2A = 12; // мотор правый №2
const int MR2B = 13; // мотор правый №2
const int MR2C = 11; // ШИМ

SoftwareSerial btSerial(0, 1); // RX, TX  

String readStr;
int lastLeftDirection, lastRightDirection;

void setup() {
  //Serial.begin(9600);
  //Serial.println("Start.");

  btSerial.begin(9600);

  lastLeftDirection = 0;
  lastRightDirection = 0;
  
  pinMode(ML1C, OUTPUT);
  pinMode(ML1A, OUTPUT);
  pinMode(ML1B, OUTPUT);
  pinMode(ML2C, OUTPUT);
  pinMode(ML2A, OUTPUT);
  pinMode(ML2B, OUTPUT);
  
  pinMode(MR1C, OUTPUT);
  pinMode(MR1A, OUTPUT);
  pinMode(MR1B, OUTPUT);
  pinMode(MR2C, OUTPUT);
  pinMode(MR2A, OUTPUT);
  pinMode(MR2B, OUTPUT);
}

void loop() {
  
  delay(200);
  readStr="";

  while (btSerial.available())
  {
    delay(3);  //delay to allow buffer to fill 
    if (btSerial.available() >0)
    {
      char c = btSerial.read();  //gets one byte from serial buffer
      readStr += c; //makes the string readString
    } 
  }
  
  if (readStr.length() > 0)
  {
    //Serial.println(readStr);
    
    int leftSpeedValue = 0;
    int rightSpeedValue = 0;

    int leftDirection = readStr[0] == '0' ? _BACKWARD : _FORWARD;
    leftSpeedValue = readStr.substring(1, 2).toInt();
    int rightDirection = readStr[3] == '0' ? _BACKWARD : _FORWARD;
    rightSpeedValue = readStr.substring(4, 5).toInt();

    // min speed value is 155 and max is 255,
    // by BT we are wating value from 0 to 99,
    // so wee need to sum up 155 and value from BT
    setSpeed(LEFT, leftDirection, 155 + leftSpeedValue);
    setSpeed(RIGHT, rightDirection, 155 + rightSpeedValue);
  }
}

void setSpeed(int side, int _direction, int value)
{
  if (side == LEFT)
  {
    if (_direction != lastLeftDirection)
    {
      setSideSpeed(ML1C, ML2C, 0);
      delay(DIRECTION_CHANGE_DELAY);
      setDirection(ML1A, ML1B, ML2A, ML2B, _direction);
    }
    setSideSpeed(ML1C, ML2C, value);
    lastLeftDirection = _direction;
  }
  else if (side == RIGHT)
  {
    if (_direction != lastRightDirection)
    {
      setSideSpeed(MR1C, MR2C, 0);
      delay(DIRECTION_CHANGE_DELAY);
      setDirection(MR1A, MR1B, MR2A, MR2B, _direction);
    }
    setSideSpeed(MR1C, MR2C, value);
    lastRightDirection = _direction;
  }
}

void setDirection(int a1, int b1, int a2, int b2, bool isForward)
{
  if (isForward)
  {
    digitalWrite (a1, LOW);
    digitalWrite (b1, HIGH);
    digitalWrite (a2, LOW);
    digitalWrite (b2, HIGH); 
  }
  else
  {
    digitalWrite (b1, LOW);
    digitalWrite (a1, HIGH);
    digitalWrite (b2, LOW);
    digitalWrite (a2, HIGH); 
  }
}

void setSideSpeed(int c1, int c2, int speedValue)
{
    analogWrite(c1, speedValue);
    analogWrite(c2, speedValue);
}

