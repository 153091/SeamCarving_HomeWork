import edu.princeton.cs.algs4.Picture;

public class SeamCarver {

    private int height;
    private int width;
    private static final double BORDER = 1000.0; // energy of the border
    private double[][] dualGradientEnergy; // energy of pixels [width][height]
    private int[][] pixelsRGBofPicture; // two-dimensional array of RGB of pixels


    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException("argument for constructor is NULL");
        }

        Picture pictureCopy = new Picture(picture);

        height = pictureCopy.height();
        width = pictureCopy.width();

        // dual-gradient energy function
        dualGradientEnergy = new double[width][height];
        pixelsRGBofPicture = new int[width][height];

            // set RGB for each pixel
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                pixelsRGBofPicture[i][j] = pictureCopy.getRGB(i, j);

            // 1000 energy for border pixels
            // and compute energy for others

            // top and bot 1000
        for (int column = 0; column < width; column++) {
            dualGradientEnergy[column][0] = BORDER;
            dualGradientEnergy[column][height - 1] = BORDER;
        }
            // left and right 1000
        for (int row = 0; row < height; row++) {
            dualGradientEnergy[0][row] = BORDER;
            dualGradientEnergy[width - 1][row] = BORDER;
        }
            // other pixels
        for (int column = 1; column < width - 1; column++)
            for (int row = 1; row < height - 1; row++)
                dualGradientEnergy[column][row] = computeEnergy(column, row);
    }

    // private method for compute energy of pixel
    private double computeEnergy(int width, int height) {
        int rgbU = pixelsRGBofPicture[width][height - 1];
        int rgbD = pixelsRGBofPicture[width][height + 1];
        int rgbL = pixelsRGBofPicture[width - 1][height];
        int rgbR = pixelsRGBofPicture[width + 1][height];

        // this from specification
        int rU = (rgbU >> 16) & 0xFF;
        int gU = (rgbU >>  8) & 0xFF;
        int bU = (rgbU >>  0) & 0xFF;
        int rD = (rgbD >> 16) & 0xFF;
        int gD = (rgbD >>  8) & 0xFF;
        int bD = (rgbD >>  0) & 0xFF;
        int rL = (rgbL >> 16) & 0xFF;
        int gL = (rgbL >>  8) & 0xFF;
        int bL = (rgbL >>  0) & 0xFF;
        int rR = (rgbR >> 16) & 0xFF;
        int gR = (rgbR >>  8) & 0xFF;
        int bR = (rgbR >>  0) & 0xFF;

        int rX = rR - rL;
        int gX = gR - gL;
        int bX = bR - bL;
        int deltaX = (rX * rX) + (gX * gX) + (bX * bX);
        int rY = rD - rU;
        int gY = gD - gU;
        int bY = bD - bU;
        int deltaY = (rY * rY) + (gY * gY) + (bY * bY);

        return Math.sqrt(deltaX + deltaY);

    }

    // current picture
    public Picture picture() {

    }

    // width of current picture
    public int width()

    // height of current picture
    public int height()

    // energy of pixel at column x and row y
    public double energy(int x, int y)

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam()

    // sequence of indices for vertical seam
    public int[] findVerticalSeam()

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam)

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam)

    //  unit testing (optional)
    public static void main(String[] args)

}