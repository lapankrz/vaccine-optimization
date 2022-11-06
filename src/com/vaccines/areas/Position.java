package com.vaccines.areas;

public class Position {
    public int x, y;

    public Position(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public double getDistance(Position pos)
    {
        return Math.sqrt((x - pos.x) * (x - pos.x) + (y - pos.y) * (y - pos.y));
    }
}
