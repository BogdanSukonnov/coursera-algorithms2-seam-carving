/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.Picture;

import java.util.Arrays;

public class SeamCarver {

    private static final double FRAME_ENERGY = 1000.0;

    private static final double UNKNOWN_ENERGY = -1;

    private int[] points;

    private double[] energy;

    private int width;

    private int height;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException();
        }
        this.width = picture.width();
        this.height = picture.height();
        this.points = new int[picture.width() * picture.height()];
        this.energy = new double[picture.width() * picture.height()];
        Arrays.fill(this.energy, UNKNOWN_ENERGY);
        for (int point = 0; point < this.points.length; ++point) {
            this.points[point] = picture.getRGB(x(point, false), y(point, false));
        }
    }

    // current picture
    public Picture picture() {
        Picture picture = new Picture(width, height);
        for (int point = 0; point < width * height; point++) {
            picture.setRGB(x(point, false), y(point, false), points[point]);
        }
        return picture;
    }

    // width of current picture
    public int width() {
        return width;
    }

    // height of current picture
    public int height() {
        return height;
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException();
        }
        if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
            return FRAME_ENERGY;
        }
        int flatIndex = toFlatIndex(x, y);
        if (energy[flatIndex] != UNKNOWN_ENERGY) {
            return energy[flatIndex];
        }
        int left = points[toFlatIndex(x - 1, y)];
        int right = points[toFlatIndex(x + 1, y)];
        int top = points[toFlatIndex(x, y - 1)];
        int bottom = points[toFlatIndex(x, y + 1)];
        energy[flatIndex] = Math.sqrt(getDelta(left, right) + getDelta(top, bottom));
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

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        checkSeam(seam, true);
        for (int seamIndex = 0; seamIndex < seam.length; ++seamIndex) {
            // real coordinates, don't need to rotate
            removePointHorizontal(seamIndex, seam[seamIndex]);
        }
        --height;
        Arrays.fill(energy, UNKNOWN_ENERGY);
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] inputSeam) {
        checkSeam(inputSeam, false);
        int[] seam = new int[inputSeam.length];
        for (int y = 0; y < seam.length; y++) {
            seam[y] = rotatedIndex(y * rowSize(false) + inputSeam[y], false);
        }
        Arrays.sort(seam);
        for (int i = 0; i < seam.length; ++i) {
            removePointVertical(i, seam);
        }
        --width;
        Arrays.fill(energy, UNKNOWN_ENERGY);
    }

    //  unit testing (optional)
    public static void main(String[] args) {
        // no tests
    }

    private int[] findSeam(boolean isHorizontal) {
        double[] dist = new double[width * height];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(dist, 0, isHorizontal ? height - 1 : width - 1, FRAME_ENERGY);

        int[] prev = new int[width * height];

        for (int point = 0; point < width * height; ++point) {
            relaxPoint(point, dist, prev, isHorizontal);
        }

        return getSeam(dist, prev, isHorizontal);
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

    private void removePointHorizontal(int x, int y) {
        for (int yToMove = y + 1; yToMove < height; ++yToMove) {
            points[toFlatIndex(x, yToMove - 1)] =
                    points[toFlatIndex(x, yToMove)];
        }
    }

    private void removePointVertical(int seamIndex, int[] seam) {
        int next = seamIndex == seam.length - 1 ? seam.length : seam[seamIndex + 1];
        for (int toMove = seam[seamIndex] + 1; toMove < next; ++toMove) {
            int moveTo = toMove - seamIndex - 1;
            points[moveTo] = points[toMove];
        }
    }

    private double energy(int flatIndex, boolean isHorizontal) {
        return energy(x(flatIndex, isHorizontal), y(flatIndex, isHorizontal));
    }

    private int x(int flatIndex, boolean isHorizontal) {
        return rotatedIndex(flatIndex, isHorizontal) % width;
    }

    private int y(int flatIndex, boolean isHorizontal) {
        return rotatedIndex(flatIndex, isHorizontal) / width;
    }

    private int rotatedIndex(int flatIndex, boolean isHorizontal) {
        if (isHorizontal) {
            return toFlatIndex(flatIndex / height, flatIndex % height);
        }
        else {
            return flatIndex;
        }
    }

    private void relaxPoint(int point, double[] dist, int[] prev, boolean isHorizontal) {
        int rowSize = rowSize(isHorizontal);
        if (point + rowSize >= width * height) {
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
        return isHorizontal ? height : width;
    }

    private void checkSeam(int[] seam, boolean isHorizontal) {
        int expectedLength = rowSize(!isHorizontal);
        if (seam == null || seam.length != expectedLength || expectedLength < 2) {
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

        int getShift() {
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
        return (deltaRedH * deltaRedH) + (deltaGreenH * deltaGreenH) + (deltaBlueH
                * deltaBlueH);
    }

    private int toFlatIndex(int x, int y) {
        return (y * rowSize(false)) + x;
    }
}
