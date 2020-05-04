/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.Picture;

import java.util.Arrays;

public class SeamCarver {

    public static final double FRAME_ENERGY = 1000.0;

    public static final double UNKNOWN_ENERGY = -1;

    private Picture picture;

    private double energy[];

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException();
        }
        this.picture = new Picture(picture);
        energy = new double[picture.width() * picture.height()];
        Arrays.fill(energy, UNKNOWN_ENERGY);
    }

    // current picture
    public Picture picture() {
        return new Picture(picture);
    }

    // width of current picture
    public int width() {
        return picture.width();
    }

    // height of current picture
    public int height() {
        return picture.height();
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || x >= width() || y < 0 || y >= height()) {
            throw new IllegalArgumentException();
        }
        if (x == 0 || x == width() - 1 || y == 0 || y == height() - 1) {
            return FRAME_ENERGY;
        }
        int flatIndex = toFlatIndex(x, y);
        if (energy[flatIndex] != UNKNOWN_ENERGY) {
            return energy[flatIndex];
        }
        int deltaH = getDelta(picture.getRGB(x - 1, y), picture.getRGB(x + 1, y));
        int deltaV = getDelta(picture.getRGB(x, y - 1), picture.getRGB(x, y + 1));
        energy[flatIndex] = Math.sqrt(deltaH + deltaV);
        return energy[flatIndex];
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        return findSeam(true);
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        return findSeam(false);
    }

    private int[] findSeam(boolean isHorizontal) {
        double[] dist = new double[energy.length];
        Arrays.fill(dist, Double.MAX_VALUE);
        Arrays.fill(dist, 0, isHorizontal ? height() - 1 : width() - 1, FRAME_ENERGY);

        int[] prev = new int[energy.length];

        for (int point = 0; point < energy.length; ++point) {
            relaxPoint(point, dist, prev, isHorizontal);
        }

        return getSeam(dist, prev, isHorizontal);
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        checkSeam(seam, picture.width());
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        checkSeam(seam, picture.height());

    }

    //  unit testing (optional)
    public static void main(String[] args) {
    }

    private int[] getSeam(double[] dist, int[] prev, boolean isHorizontal) {
        int lastMin = indexOfMinDistInLastRow(dist, isHorizontal);
        int[] seam = new int[rowSize(!isHorizontal)];
        for (int i = seam.length - 1; i >= 0; --i) {
            seam[i] = lastMin % rowSize(isHorizontal);
            lastMin = prev[lastMin];
        }
        return seam;
    }

    private int indexOfMinDistInLastRow(double[] dist, boolean isHorizontal) {
        double minDistInLastRow = Integer.MAX_VALUE;
        int indexOfMinDistInLastRow = 0;
        for (int index = dist.length - rowSize(isHorizontal); index < dist.length; index++) {
            if (dist[index] < minDistInLastRow) {
                minDistInLastRow = dist[index];
                indexOfMinDistInLastRow = index;
            }
        }
        return indexOfMinDistInLastRow;
    }

    private double energy(int flatIndex, boolean isHorizontal) {
        return energy(x(flatIndex, isHorizontal), y(flatIndex, isHorizontal));
    }

    private int x(int flatIndex, boolean isHorizontal) {
        return rotatedIndex(flatIndex, isHorizontal) % width();
    }

    private int y(int flatIndex, boolean isHorizontal) {
        return rotatedIndex(flatIndex, isHorizontal) / width();
    }

    private int rotatedIndex(int flatIndex, boolean isHorizontal) {
        if (isHorizontal) {
            return toFlatIndex(flatIndex / height(), flatIndex % height());
        }
        else {
            return flatIndex;
        }
    }

    private void relaxPoint(int point, double[] dist, int[] prev, boolean isHorizontal) {
        int rowSize = rowSize(isHorizontal);
        if (point + rowSize >= energy.length) {
            return;
        }
        for (int deltaX = -1; deltaX <= 1; ++deltaX) {
            if (point % rowSize == 0 && deltaX < 0
                    || point % rowSize == rowSize - 1 && deltaX > 0) {
                continue;
            }
            int nextPoint = point + rowSize + deltaX;
            double newDist = dist[point] + energy(nextPoint, isHorizontal);
            if (newDist < dist[nextPoint]) {
                dist[nextPoint] = newDist;
                prev[nextPoint] = point;
            }
        }
    }

    private int rowSize(boolean isHorizontal) {
        return isHorizontal ? height() : width();
    }

    private void checkSeam(int[] seam, int length) {
        if (seam == null || seam.length != length || length < 2) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < seam.length - 1; ++i) {
            if (Math.abs(seam[i] - seam[i + 1]) > 1) {
                throw new IllegalArgumentException();
            }
        }
    }

    private enum Color {

        RED(16), GREEN(8), BLUE(0);

        private int shift;

        private Color(int shift) {
            this.shift = shift;
        }

        private int getShift() {
            return shift;
        }
    }

    private int colorValue(int rgb, Color color) {
        return (rgb >> color.getShift()) & 0xFF;
    }

    private int getDelta(int rgb1, int rgb2) {
        int deltaRedH = colorValue(rgb1, Color.RED) - colorValue(rgb2, Color.RED);
        int deltaGreenH = colorValue(rgb1, Color.GREEN) - colorValue(rgb2, Color.GREEN);
        int deltaBlueH = colorValue(rgb1, Color.BLUE) - colorValue(rgb2, Color.BLUE);
        return (deltaRedH * deltaRedH) + (deltaGreenH * deltaGreenH) + (deltaBlueH * deltaBlueH);
    }

    private int toFlatIndex(int x, int y) {
        return (y * width()) + x;
    }
}
