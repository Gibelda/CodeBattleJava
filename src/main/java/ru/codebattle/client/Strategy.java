package ru.codebattle.client;

import ru.codebattle.client.api.BoardElementWithWeight;
import ru.codebattle.client.api.BoardPoint;
import ru.codebattle.client.api.Direction;
import ru.codebattle.client.api.GameBoard;

import java.util.*;

import static ru.codebattle.client.api.BoardElementWithWeight.*;
import static ru.codebattle.client.api.BoardElementWithWeight.ENEMY_HEAD_LEFT;

public class Strategy {
    static int evilCount = 0;
    static boolean preEvil = false;
    static Direction prevDirection;

    public static boolean chooseAct(GameBoard board) {
        // TODO а это вообще актуально?
        if (board.getMyHead() != null && board.getMyTail() != null)
            return evilCount > 20 &&
                Math.abs(board.getMyHead().getY() - board.getMyTail().getY())
                        + Math.abs(board.getMyHead().getX() - board.getMyTail().getX()) < 6;
        else return false;
    }

    public static Direction chooseRightDirection(GameBoard board) {
        BoardPoint head = board.getMyHead();
        if (head == null || board.getElementAt(head) == BoardElementWithWeight.HEAD_SLEEP) {
            evilCount = head == null ? evilCount - 1 : 0;
            prevDirection = Direction.STOP;
            return Direction.STOP;
        }
        if (preEvil) {
            evilCount += 10;
            preEvil = false;
        }
        evilCount = evilCount == 0 ? 0 : evilCount - 1;
        // Объявляем массивы и множества для алгоритма поиска пути
        int size = board.size(); // размер стороны карты, размер всего массива size^2
        Set<BoardPoint> border = new HashSet<>(); // множество точек текущей границы
        border.add(head);
        Direction[] ways = new Direction[size * size]; // откуда пришли в эту точку
        int[] weights = new int[size * size]; // вес пути до этой точки
        boolean[] visits = new boolean[size * size]; // посещена ли эта точка
        int wayLength = 0;
        int avgWeight = 0;
        while (!border.isEmpty()) { // пока граница непуста
            int sumWeight = 0;
            HashSet<BoardPoint> neighbors = new HashSet<>(); // создаем множество точек - новая граница
            for (BoardPoint point : border) { // в этом цикле создается новая более широкая граница
                int pointIndex = point.getY() * size + point.getX();
                weights[pointIndex] = evilCount - 1 > wayLength ?
                        board.getElementAt(point).getEvilWeight() :
                        board.getElementAt(point).getWeight();
                if (weights[pointIndex] < avgWeight && weights[pointIndex] > -100) {
                    neighbors.add(point);
                    sumWeight += weights[pointIndex];
                    continue;
                }
                Map<BoardPoint, Direction> fourPoints = new HashMap<>(); // четыре точки вокруг нашей
                fourPoints.put(point.shiftRight(), Direction.RIGHT);
                fourPoints.put(point.shiftBottom(), Direction.DOWN);
                fourPoints.put(point.shiftLeft(), Direction.LEFT);
                fourPoints.put(point.shiftTop(), Direction.UP);
                for (BoardPoint newPoint : fourPoints.keySet()) { // для этих четырех
                    int index = newPoint.getY() * size + newPoint.getX(); // индекс на карте
                    if (newPoint.getX() > 0 && newPoint.getY() > 0 && newPoint.getX() < size && newPoint.getY() < size
                            && !visits[index]) {
                        weights[index] = evilCount - 1 > wayLength ?
                                board.getElementAt(newPoint).getEvilWeight() :
                                board.getElementAt(newPoint).getWeight();
                        if (wayLength == 0 &&
                                (prevDirection == Direction.RIGHT && fourPoints.get(newPoint) == Direction.LEFT ||
                                 prevDirection == Direction.LEFT && fourPoints.get(newPoint) == Direction.RIGHT ||
                                 prevDirection == Direction.UP && fourPoints.get(newPoint) == Direction.DOWN ||
                                 prevDirection == Direction.DOWN && fourPoints.get(newPoint) == Direction.UP))
                            weights[index] = -100;
                        if (weights[index] <= weights[index] + weights[pointIndex]
                            && weights[index] > -100) {
                            weights[index] = weights[index] + weights[pointIndex];
                            ways[index] = fourPoints.get(newPoint);
                            sumWeight += weights[index];
                            neighbors.add(newPoint);
                        }
                    }
                    if (wayLength == 0 && board.hasElementAt(newPoint, FURY_PILL))
                        preEvil = true;
                }
            }
            // тут отмечаем, что все точки новой границы посещены
            int maxWeight = avgWeight;
            int maxIndex = -1;
            for (BoardPoint newPoint : border) {
                int index = newPoint.getY() * size + newPoint.getX(); // индекс на карте
                visits[index] = true;
                if (maxWeight < weights[index]) {
                    maxWeight = weights[index];
                    maxIndex = index;
                }
            }
            if (maxIndex >= 0 && (maxWeight > 0 || (neighbors.isEmpty() && maxWeight >= avgWeight))) {
                myPrint(size, ways, maxIndex, head, board);
                System.out.println(evilCount);
                Direction curDirection = Direction.STOP;
                BoardPoint tmpPoint = new BoardPoint(maxIndex % size, maxIndex / size);
                // TODO тут пока так, но надо: обратный путь
                while (!tmpPoint.equals(head)) {
                    curDirection = ways[tmpPoint.getY() * size + tmpPoint.getX()];
                    switch (curDirection) {
                        case UP:
                            tmpPoint = tmpPoint.shiftBottom();
                            break;
                        case DOWN:
                            tmpPoint = tmpPoint.shiftTop();
                            break;
                        case LEFT:
                            tmpPoint = tmpPoint.shiftRight();
                            break;
                        case RIGHT:
                            tmpPoint = tmpPoint.shiftLeft();
                            break;
                    }
                }
                prevDirection = curDirection;
                return curDirection;
            }
            border = neighbors;
            wayLength++;
            avgWeight = border.size() > 0 ? sumWeight / border.size() : 0;
        }
        return Direction.STOP;
    }

    private static void myPrint(int size, Direction[] ways, int index, BoardPoint head, GameBoard board) {
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                String s = "_";
                if (ways[j * size + i] == Direction.UP) s = "^";
                if (ways[j * size + i] == Direction.DOWN) s = "v";
                if (ways[j * size + i] == Direction.LEFT) s = "<";
                if (ways[j * size + i] == Direction.RIGHT) s = ">";
                if (j * size + i == index) s = "$";
                if (i * size + j == head.getX() * size + head.getY()) s = "S";
                if (board.hasElementAt(new BoardPoint(i, j), ENEMY_HEAD_EVIL, ENEMY_HEAD_FLY, ENEMY_HEAD_UP,
                        ENEMY_HEAD_RIGHT, ENEMY_HEAD_DOWN, ENEMY_HEAD_LEFT)) s = "Y";
                //if (board.hasElementAt(new BoardPoint(i, j), WALL)) s = "#";
                if (board.hasElementAt(new BoardPoint(i, j), STONE)) s = "o";
                System.out.print(s + " ");
            }
            System.out.println();
        }
    }
}
