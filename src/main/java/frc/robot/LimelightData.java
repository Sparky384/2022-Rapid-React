package frc.robot;

public class LimelightData {

    /*
    Measured Distance
    The shortest horizontal distance from the front bumper of the robot to a vertical line projected from the vision target to the floor,
    as measured by a tape measure
    */
    public final double measuredDistance[] = {
        45,55,65,76,86,96,106,116,126,136,146,156,166,176
    };

    /*
    Raw Distance
    As measured by Limelight
    Based on the calculateDistance() method in the Limelight class 
    */
    public final double rawDistance[] = {
        57.8,69.3,81.1,94.2,105.8,116.4,126.6,134.7,149.1,161.4,172.8,182.3,196.5,210.4    
    };

    /*
    Corrected final distance
    Subtracted 30 (average error) from measuredDistance to get this. Note that this is an approximation,
    but it's a pretty good one.  The offset was selected to minimize the error of the far end of 
    the shooting range, at the expense of the close end.  Better results could be obtained by subtracting
    a correction "curve" which is a function of distance, and not just a straight line.
    
    Hopefully we won't need this anymore.
    */
    public final double finalDistance[] = {
        35.8,47.3,61.1,74.2,85.8,96.4,106.6,114.7,129.1,141.4,152.8,162.3,176.5,190.4
    };

/*
    Limelight Y angle values at each measured distance value
    I didn't take as many data points as needed, so this will need to be fixed
    */
    public final double targetYAngle[] = {
        21.12,16.0,11.5,7.2,4.1,1.6,-0.4,-1.96,-4.3,-6.0,-7.4,-8.5,-9.95,-11.2 
    };

    /*
    RPM values for best shot at each measured distance value
    */
    public final double rpm[] = {
        //1950,2050,2175,2300,2375,2450,2525,2600,2700,2800,3000,3200,3375
        // The first value, 1500, is just a guess I made and should be replaced
        // with real value
        //1500,1950,2050,2175,2300,2375,2450,2525,2600,2700,2800,3000,3200,3375
       // 1400,1600,2050,2175,2300,2375,2450,2525,2600,2700,2800,3000,3200,3375
       1400,1600,2050,2175,2300,2375,2450,2525,2600,2600,2700,2900,3100,3275
    };

    // Just for convenience
    int numPoints = measuredDistance.length;

     /*
    I got this from the internet - seems to work really well
    Note that both xa[] and ya[] arrays must be the same size, or weird
    results can occur.

    I tested this method to make sure it extrapolates as well as it interpolates,
    and it did for the values I gave it.
    */
    public double interpolate(double xa[], double ya[], double x) {
        /*
        Given arrays xa[1..n] and ya[1..n], and given a value x, 
        this routine returns a value y. 
        If P(x) is the polynomial of degree N ? 1 
        such that P(xa[i]) = ya[i]; 
        i = 1...n, then the returned value y = P(x).
         */

        if (xa.length != ya.length || xa.length == 0 || ya.length == 0) {
            System.out.println("** Invalid Parameter");
            return Double.NaN;
        }

        int n = xa.length;
        double y = 0.0;
        double dy = 0.0;

        int i, m, ns = 1;
        double den, dif, dift, ho, hp, w;
        double[] c = new double[n];
        double[] d = new double[n];
        dif = Math.abs(x - xa[0]);

        for (i = 0; i < n; i++) { // Here we find the index ns of the closest table entry,
            if ((dift = Math.abs(x - xa[i])) < dif) {
                ns = i;
                dif = dift;
            }
            c[i] = ya[i]; // and initialize the tableau of c's and d's.
            d[i] = ya[i];
        }

        y = ya[ns--]; // This is the initial approximation to y.
        //System.out.println("** y ~ "+y);

        for (m = 0; m < n - 1; m++) { // For each column of the tableau,
            for (i = 0; i < n - m - 1; i++) { // we loop over the current c's and d's and update them. 

                //System.out.println("** m = "+m+", i = "+i);
                ho = xa[i] - x;
                hp = xa[i + m + 1] - x;
                w = c[i + 1] - d[i];

                if ((den = ho - hp) == 0.0) {
                    return Double.NaN;
                }
                // This error can occur only if two input xa's are (to within roundof identical.

                //System.out.println("** ho = "+ho+", hp = "+hp);

                den = w / den;
                d[i] = hp * den; // Here the c's and d's are updated.
                c[i] = ho * den;
                //System.out.println("** c[i] = "+c[i]+", d[i] = "+d[i]);
            }

            y += (dy = (2 * (ns + 1) < (n - m) ? c[ns + 1] : d[ns--]));
            //System.out.println("** dy = "+dy+", y = "+y);

            /*
            After each column in the tableau is completed, we decide which correction, c or d,
            we want to add to our accumulating value of y, i.e., which path to take through the
            tableau forking up or down. We do this in such a way as to take the most "straight
            line" route through the tableau to its apex, updating ns accordingly to keep track of
            where we are. This route keeps the partial approximations centered (insofar as possible)
            on the target x. The last dy added is thus the error indication.
            */
        }
        return y;
    }


}
