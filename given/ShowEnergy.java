package given; /******************************************************************************
 *  Compilation:  javac given.ShowEnergy.java
 *  Execution:    java given.ShowEnergy input.png
 *  Dependencies: SeamCarver.java given.SCUtility.java
 *
 *
 *  Read image from file specified as command line argument. Show original
 *  image (only useful if image is large enough).
 *
 ******************************************************************************/

import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;

public class ShowEnergy {

    public static void main(String[] args) {
        Picture picture = new Picture(args[0]);
        StdOut.printf("image is %d columns by %d rows\n", picture.width(), picture.height());
        picture.show();
        SeamCarver sc = new SeamCarver(picture);

        StdOut.printf("Displaying energy calculated for each pixel.\n");
        SCUtility.showEnergy(sc);

    }

}
