package package1;

import java.util.*;
import java.util.LinkedList;
import java.util.Queue;

public class BFS {
    private int[][] matrix;
    private int[][] visited;
    private int startRow;
    private int startCol;
    private int targetRow;
    private int targetCol;

    public BFS(int[][] matrix, int startRow, int startCol, int targetRow, int targetCol) {
        this.matrix = matrix;
        this.visited = new int[matrix.length][matrix[0].length];
        this.startRow = startRow;
        this.startCol = startCol;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
    }

    public List<int[]> findPath() {
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{startRow, startCol});
        visited[startRow][startCol] = 1;

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Atas, Bawah, Kiri, Kanan

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int row = current[0];
            int col = current[1];

            if (row == targetRow && col == targetCol) {
                // Path ditemukan
                List<int[]> path = new ArrayList<>();
                path.add(new int[]{row, col});

                while (row != startRow || col != startCol) {
                    for (int[] direction : directions) {
                        int newRow = row + direction[0];
                        int newCol = col + direction[1];

                        if (newRow >= 0 && newRow < matrix.length && newCol >= 0 && newCol < matrix[0].length &&
                                visited[newRow][newCol] == visited[row][col] - 1) {
                            path.add(new int[]{newRow, newCol});
                            row = newRow;
                            col = newCol;
                            break;
                        }
                    }
                }

                Collections.reverse(path);
                return path;
            }

            for (int[] direction : directions) {
                int newRow = row + direction[0];
                int newCol = col + direction[1];

                if (newRow >= 0 && newRow < matrix.length && newCol >= 0 && newCol < matrix[0].length &&
                        matrix[newRow][newCol] == 0 && visited[newRow][newCol] == 0) {
                    queue.offer(new int[]{newRow, newCol});
                    visited[newRow][newCol] = visited[row][col] + 1;
                }
            }
        }

        return new ArrayList<>(); // Tidak ada path yang ditemukan
    }
}
