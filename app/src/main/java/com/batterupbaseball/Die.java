package com.batterupbaseball;

public class Die
{

    private final int MAX = 10;  // maximum face value

    private int faceValue;  // current value showing on the die

    //  Constructor: Sets the initial face value.
    public Die()
    {
        this.faceValue = 0;
    }

    // Alternate Constructor allowing user to set the face value
    public Die(int value)
    {
        this.faceValue = value;
    }

    //  Rolls the die and returns the result.
    public int roll()
    {
        this.faceValue = (int)(Math.random() * MAX);

        return this.faceValue;
    }

    //  Face value setter.
    public void setFaceValue (int value)
    {
        this.faceValue = value;
    }

    //  Face value getter.
    public int getFaceValue()
    {
        return this.faceValue;
    }

}