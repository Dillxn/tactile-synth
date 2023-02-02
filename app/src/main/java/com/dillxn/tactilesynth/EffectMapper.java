package com.dillxn.tactilesynth;

public class EffectMapper {
    /*
    Rows going down: X, Y, Z
    Columns left to right: Voices, Filter, Reverb, BitCrush

    A default value of 5 signifies a slot not being used. To change which axis controls which effect.
    Change where the 1's are placed using the above rows and columns
     */
    double map[][] = {{5,5,5,1},
                      {5,5,5,1},
                      {5,5,5,1}};
    public EffectMapper(){
    }

    //returns the value of an effect given it's column
    public double getValue(int column){
        for(int i = 0; i < map.length; i++){
            if(map[i][column] != 5){
                return map[i][column];
            }
        }
        return 0;
    }
    // updates the x values for effects that map to that axis
    public void setX(double x){
        for(int i = 0; i < map[0].length; i++){
            if(map[0][i] != 5){
                map[0][i] = x;
            }
        }
    }
    //updates the y values for effects that map to that axis
    public void setY(double y){
        for(int i = 0; i < map[1].length; i++){
            if(map[1][i] != 5){
                map[1][i] = y;
            }
        }
    }
    //updates the z values for effects that map to that axis
    public void setZ(double z){
        for(int i = 0; i < map[2].length; i++){
            if(map[2][i] != 5){
                map[2][i] = z;
            }
        }
    }
    //this method is used when you want to change a specific effect on an axis without effecting others
    //on that axis
    public void setSpecific(int row, int column, double d){
        map[row][column] = d;
    }

    //for changing which axis an effect maps to
    public void updateColumn(int column, int newAxis){
        for(int i = 0; i < map.length; i++){
            map[i][column] = 5;
        }
        map[newAxis][column] = 1;
    }
}
