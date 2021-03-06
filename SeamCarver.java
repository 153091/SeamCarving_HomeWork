import edu.princeton.cs.algs4.Picture;

public class SeamCarver {

    private int height;
    private int width;
    private static final double BORDER = 1000.0; // energy of the border
    private double[][] dualGradientEnergy; // [width][height] energy of pixels
    private int[][] pixelRGBofPicture; // [width][height] array of RGB of pixels
    private boolean horizontal = true; // orientation of the picture


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
        pixelRGBofPicture = new int[width][height];

            // set RGB for each pixel
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                pixelRGBofPicture[i][j] = pictureCopy.getRGB(i, j);

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
        int rgbU = pixelRGBofPicture[width][height - 1];
        int rgbD = pixelRGBofPicture[width][height + 1];
        int rgbL = pixelRGBofPicture[width - 1][height];
        int rgbR = pixelRGBofPicture[width + 1][height];

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
        Picture currentPicture = new Picture(width, height);

        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                if (horizontal) currentPicture.setRGB(i, j, pixelRGBofPicture[i][j]);
                else            currentPicture.setRGB(i, j, pixelRGBofPicture[j][i]);
            }

        return currentPicture;
    }

    // width of current picture
    public int width() {
        return width;
    }

    // height of current picture
    public int height() {
        return height;
    }

    // energy of pixel at column(width) x and row(height) y
    public double energy(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("energy of invalid pixel");
        }

        if (horizontal) return dualGradientEnergy[x][y];
        else            return dualGradientEnergy[y][x];
    }

    // transpose to vertical
    private void transposeV() {
        if (horizontal) {
            // create empty arrays for RGB and Energy
            double[][] tempEnergy = new double[height][width];
            int[][] tempRGB = new int[height][width];

            // insert values
            for (int i = 0; i < height; i++)
                for (int j = 0; j < width; j++) {
                    tempEnergy[i][j] = dualGradientEnergy[j][i];
                    tempRGB[i][j] = pixelRGBofPicture[j][i];
                }
            // exch
            dualGradientEnergy = tempEnergy;
            pixelRGBofPicture = tempRGB;
            horizontal = false;
        }
    }

    // transpose to horizontal
    private void transposeH() {
        if (!horizontal) {
            // create empty arrays for RGB and Energy
            double[][] tempEnergy = new double[width][height];
            int[][] tempRGB = new int[width][height];

            // insert values
            for (int i = 0; i < width; i++)
                for (int j = 0; j < height; j++) {
                    tempEnergy[i][j] = dualGradientEnergy[j][i];
                    tempRGB[i][j] = pixelRGBofPicture[j][i];
                }
            // exch
            dualGradientEnergy = tempEnergy;
            pixelRGBofPicture = tempRGB;
            horizontal = true;
        }
    }

    // array of {x-1, x, x+1}
    private int[] scanX(int X, int last) {
        int[] scan = new int[3];
        if (X == 0) scan[0] = last;
        else        scan[0] = X - 1;
        scan[1] = X;
        if (X == last) scan[2] = 0;
        else           scan[2] = X + 1;

        return scan;
    }

    // searching for Seam (vertical Seam for horizontal orientation)
    private int[] searchSeam(int width, int height) {
        // if height > 2
        if (height > 2) {
            double[][] distTo = new double[width][height - 2];
            int[][] edgeTo = new int[width][height - 2];

            // row 1  == row 0 for distTo/edgeTo
            // присвоение distTo & edgeTo нулевой строке этих массивов
            for (int i = 0; i < width; i++) {
                distTo[i][0] = dualGradientEnergy[i][1];
                if (i == 0) edgeTo[i][0] = width - 1;
                else        edgeTo[i][0] = i -1;
            }

            // присвоение distTo & edgeTo последующим rows(строкам)
            /*row lastX:[] [] []
                         \ | /
            * row     j:  [ ]   */
            for (int j = 1; j < height - 2; j++) {
                int lastX = j - 1; // предыдущий ряд (где три пикселя)
                for (int i = 0; i < width; i++) {
                    int[] idOf3x = scanX(i, width - 1); // x-1, x, x+1
                    double min = distTo[idOf3x[0]][lastX]; // временно наименьший dist
                    int minOf3 = idOf3x[0]; // временно наименьший Х с предыдущего row
                    // looking for minimum dist from previous row
                    // and edgeTo to vertex with minimal dist
                    for (int k = 1; k < 3; k++) {
                        if (distTo[idOf3x[k]][lastX] < min) {
                            min = distTo[idOf3x[k]][lastX]; // наименьший dist
                            minOf3 = idOf3x[k];
                        }
                    }
                    // словно в relax: distTo + weight
                    distTo[i][j] = dualGradientEnergy[i][j + 1] + distTo[minOf3][lastX];
                    edgeTo[i][j] = minOf3;
                }
            }

            // Построение SPT дерева высоты distTO от 0 до h-3 по высоте
            // на деле это height от 1 по h-2
            int minOfX = 0; // временно минимальный x на последнем ряду distTo[][]
            int lastRowOfdistTo = height - 3;
            double minAll = distTo[minOfX][lastRowOfdistTo]; // временно минимальный distTo последнего ряда
            for (int i = 1; i < width; i++) {
                if (distTo[i][lastRowOfdistTo] < minAll) {
                    minAll = distTo[i][lastRowOfdistTo]; // минимальный distTo последнего ряда
                    minOfX = i; // минимальный x на последнем ряду distTo[][]
                }
            }

            // построение полного дерева SPT высотой Height [0... h-1]
            int[] seam = new int[height];
            if (minOfX == 0) {        // если минимальный Х предпоследнего ряда на позиции 0
                seam[height - 1] = 0; // то Х последнего ряда будет тоже на позиции 0
            }
            else {                    // а иначе Х последнего ряда будет на позиции Х-1
                seam[height - 1] = minOfX - 1;
            }
            seam[height - 2] = minOfX;
            // добавляем в Seam элементы из edgeTo
            for (int i = lastRowOfdistTo; i >= 0; i--) {
                minOfX = edgeTo[minOfX][i];
                seam[i] = minOfX;
            }

            return seam;
        }
        else {
            // если высотак картинки =2
            // то просто выбираем нулевой столбец
            int[] seam = new int[height];
            for (int i = 0; i < height; i++) {
                seam[i] = 0;
            }
            return seam;
        }
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        transposeV();
        int[] seam = searchSeam(height, width);
        return seam;

    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        transposeH();
        int[] seam = searchSeam(width, height);
        return seam;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException("argument for removeHorizontalSeam is null");
        }
        if (seam.length != width) {
            throw new IllegalArgumentException("invalid length of seam for Horizontal remove");
        }
        for (int i = 0; i < width; i++) {
            if ((seam[i] < 0) || (seam[i] >= height)) {
                throw new IllegalArgumentException("invalid entry in seam[i] for Horiz remove");
            }
        }
        int heightNEW = height - 1;
        for (int i = 0; i < width - 1; i++) {
            int delta = Math.abs(seam[i] - seam[i + 1]);
            if ((delta > 1) && ((delta != heightNEW) || (height <= 3))) {
                throw new IllegalArgumentException("invalid entry in seam[i] for Horiz remove");
            }
        }

        // развернуть вертикально
        transposeV();

        height = heightNEW; // widthNEW = width - 1;
        int[][] pixelRGBofPictureNEW = new int[height][width];
        // проходя через каждый ряд row
        // скопируем старрый массив в новый, но без элемента seam[i]
        // j - ряд width(height)  i - столбец height(width)
        //               [height][width]
        for (int j = 0; j < width; j++) {
            for (int i = 0; i < seam[j]; i++)
                pixelRGBofPictureNEW[i][j] = pixelRGBofPicture[i][j];
            for (int i = seam[j] + 1; i <= height; i++)
                pixelRGBofPictureNEW[i - 1][j] = pixelRGBofPicture[i][j];
        }

        // заменяем старый массив цветов на новый
        pixelRGBofPicture = pixelRGBofPictureNEW;

        // теперь разбираемся с энергией
        double[][] dualGradientEnergyNEW = new double[height][width];
        // first line full of 1000.0 (Border)
        for (int i = 0; i < height; i++)
            dualGradientEnergyNEW[i][0] = dualGradientEnergy[i][0];

        // ряды между первым и последним [1 по h-2]
        for (int j = 1; j < width - 1; j++) {
            // до элемента seam[j]
            if (seam[j] > 1) {
                for (int i = 0; i < seam[j]; i++)
                    dualGradientEnergyNEW[i][j] = dualGradientEnergy[i][j];
            }
            else dualGradientEnergyNEW[0][j] = BORDER;

            // после элемента seam[j]
            if (seam[j] < height - 1) {
                for (int i = seam[j] + 1; i <= height; i++)
                    dualGradientEnergyNEW[i - 1][j] = dualGradientEnergy[i][j];
            }

            // расставим BOARD
            // если индекс удаляемой точки == 1
            if (seam[j] - 1 == 0) dualGradientEnergyNEW[seam[j] - 1][j] = BORDER;
                // если seam[j] стоял последним
            else if (seam[j] == height) dualGradientEnergyNEW[seam[j] - 1][j] = BORDER;
                // если seam[j] >=2 то для предшествующей точки (seam[j]-1) -расчет энергии
            else if (seam[j] >= 2) {
                dualGradientEnergyNEW[seam[j] - 1][j] = computeEnergy(seam[j] - 1, j);
            }
            // если seam[j] была предпоследней, то точка заменится на БОРДЕР
            if (seam[j] == height - 1) dualGradientEnergyNEW[seam[j]][j] = BORDER;
                // if seam[j] < height-1, то расчет энергии для точки, которая займет место seam
            else if ((seam[j] >=1) && (seam[j] < height - 1)) {
                dualGradientEnergyNEW[seam[j]][j] = computeEnergy(seam[j], j);
            }
        }
        // last line full of 1000.0 (Border)
        for (int i = 0; i < height; i++)
            dualGradientEnergyNEW[i][width - 1] = dualGradientEnergy[i][width - 1];

        // заменяем старый массив энергии на новый
        dualGradientEnergy = dualGradientEnergyNEW;

    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException("argument for removeVerticalSeam is null");
        }
        if (seam.length != height) {
            throw new IllegalArgumentException("invalid length of seam for Vertical remove");
        }
        for (int i = 0; i < height; i++) {
            if ((seam[i] < 0) || (seam[i] >= width)) {
                throw new IllegalArgumentException("invalid entry in seam[i] for Vert remove");
            }
        }
        int widthNEW = width - 1;
        for (int i = 0; i < height - 1; i++) {
            int delta = Math.abs(seam[i] - seam[i + 1]);
            if ((delta > 1) && ((delta != widthNEW) || (width <= 3))) {
                throw new IllegalArgumentException("invalid entry in seam[i] for Vert remove");
            }
        }

        // развернуть горизонтально
        transposeH();

        width = widthNEW; // widthNEW = width - 1;
        int[][] pixelRGBofPictureNEW = new int[width][height];
        // проходя через каждый ряд row
        // скопируем старрый массив в новый, но без элемента seam[i]
        // j - ряд (height)  i - столбец (width)
        //               [width][height]
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < seam[j]; i++)
                pixelRGBofPictureNEW[i][j] = pixelRGBofPicture[i][j];
            for (int i = seam[j] + 1; i <= width; i++)
                pixelRGBofPictureNEW[i - 1][j] = pixelRGBofPicture[i][j];
        }

        // заменяем старый массив цветов на новый
        pixelRGBofPicture = pixelRGBofPictureNEW;

        // теперь разбираемся с энергией
        double[][] dualGradientEnergyNEW = new double[width][height];
            // first line full of 1000.0 (Border)
            for (int i = 0; i < width; i++)
                dualGradientEnergyNEW[i][0] = dualGradientEnergy[i][0];

            // ряды между первым и последним [1 по h-2]
            for (int j = 1; j < height - 1; j++) {
                // до элемента seam[j]
                if (seam[j] > 1) {
                    for (int i = 0; i < seam[j]; i++)
                        dualGradientEnergyNEW[i][j] = dualGradientEnergy[i][j];
                }
                else dualGradientEnergyNEW[0][j] = BORDER;

                // после элемента seam[j]
                if (seam[j] < width - 1) {
                    for (int i = seam[j] + 1; i <= width; i++)
                        dualGradientEnergyNEW[i - 1][j] = dualGradientEnergy[i][j];
                }

                // расставим BOARD
                // если индекс удаляемой точки == 1
                if (seam[j] - 1 == 0) dualGradientEnergyNEW[seam[j] - 1][j] = BORDER;
                // если seam[j] стоял последним
                else if (seam[j] == width) dualGradientEnergyNEW[seam[j] - 1][j] = BORDER;
                // если seam[j] >=2 то для предшествующей точки (seam[j]-1) -расчет энергии
                else if (seam[j] >= 2) {
                    dualGradientEnergyNEW[seam[j] - 1][j] = computeEnergy(seam[j] - 1, j);
                }
                // если seam[j] была предпоследней, то точка заменится на БОРДЕР
                if (seam[j] == width - 1) dualGradientEnergyNEW[seam[j]][j] = BORDER;
                // if seam[j] < width-1, то расчет энергии для точки, которая займет место seam
                else if ((seam[j] >=1) && (seam[j] < width - 1)) {
                    dualGradientEnergyNEW[seam[j]][j] = computeEnergy(seam[j], j);
                }
            }
            // last line full of 1000.0 (Border)
            for (int i = 0; i < width; i++)
                dualGradientEnergyNEW[i][height - 1] = dualGradientEnergy[i][height - 1];

        // заменяем старый массив энергии на новый
        dualGradientEnergy = dualGradientEnergyNEW;
    }

    //  unit testing (optional)
    public static void main(String[] args) {}

}