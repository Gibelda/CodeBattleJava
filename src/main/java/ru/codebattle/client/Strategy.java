package ru.codebattle.client;

import ru.codebattle.client.api.BoardPoint;
import ru.codebattle.client.api.Direction;
import ru.codebattle.client.api.GameBoard;

import java.util.*;

import static ru.codebattle.client.api.BoardElementWithWeight.*;

public class Strategy {
    static int evilCount = 0;
    static boolean preEvil = false;
    static Direction prevDirection;

    static final int BAD_WEIGHT = -1000;

    public static boolean chooseAct(GameBoard board) {
        if (board.getMyHead() != null && board.getMyTail() != null)
            return (evilCount > 20 &&
                Math.abs(board.getMyHead().getY() - board.getMyTail().getY())
                        + Math.abs(board.getMyHead().getX() - board.getMyTail().getX()) < 6);
        else return false;
    }

    public static Direction chooseRightDirection(GameBoard board) {
        BoardPoint head = board.getMyHead();
        if (head == null || board.getElementAt(head) == HEAD_SLEEP) {
            evilCount = head == null ? evilCount - 1 : 0;
            prevDirection = Direction.STOP;
            return Direction.STOP;
        }
        if (preEvil && board.hasElementAt(head, HEAD_EVIL)) {
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
        while (!border.isEmpty() && wayLength <= size * size) { // пока граница непуста
            int sumWeight = 0;
            HashSet<BoardPoint> neighbors = new HashSet<>(); // создаем множество точек - новая граница
            for (BoardPoint point : border) { // в этом цикле создается новая более широкая граница
                // если точка весит меньше среднего веса, но при этом не "плохая",
                // то просто добавляем ее в следующую границу и не проверяем соседей
                int pointIndex = point.getY() * size + point.getX();
                if (weights[pointIndex] < avgWeight && weights[pointIndex] > BAD_WEIGHT) {
                    neighbors.add(point);
                    sumWeight += weights[pointIndex];
                    continue;
                }
                boolean expansion = false;
                Map<BoardPoint, Direction> fourPoints = new HashMap<>(); // четыре точки вокруг нашей
                fourPoints.put(point.shiftRight(), Direction.RIGHT);
                fourPoints.put(point.shiftBottom(), Direction.DOWN);
                fourPoints.put(point.shiftLeft(), Direction.LEFT);
                fourPoints.put(point.shiftTop(), Direction.UP);
                for (BoardPoint newPoint : fourPoints.keySet()) { // для этих четырех
                    int index = newPoint.getY() * size + newPoint.getX(); // индекс на карте
                    if (newPoint.getX() > 0 && newPoint.getY() > 0 && newPoint.getX() < size && newPoint.getY() < size
                            && !visits[index]) {
                        weights[index] = getWeight(newPoint, fourPoints.get(newPoint), wayLength, board);
                        if (weights[index] <= weights[index] + weights[pointIndex] && weights[index] > BAD_WEIGHT) {
                            weights[index] += weights[pointIndex];
                            ways[index] = fourPoints.get(newPoint);
                            sumWeight += weights[index];
                            expansion = true;
                            neighbors.add(newPoint);
                        }
                    }
                    if (wayLength == 0 && board.hasElementAt(newPoint, FURY_PILL))
                        preEvil = true;
                }
                if (!expansion) // если мы из этой точки не расширились - добавим ее обратно
                    neighbors.add(point);
            }
            // тут отмечаем, что все точки новой границы посещены
            int maxWeight = avgWeight;
            int maxIndex = -1;
            for (BoardPoint newPoint : border) {
                int index = newPoint.getY() * size + newPoint.getX(); // индекс на карте
                visits[index] = true;
                if (maxWeight <= weights[index]) {
                    maxWeight = weights[index];
                    maxIndex = index;
                }
            }
            if (maxIndex >= 0 && maxWeight != 0) {
                myPrint(size, ways, maxIndex, head, board);
                System.out.println("Reward: " + maxIndex + ", weight: " + maxWeight);
                System.out.println("I am evil: " + evilCount);
                System.out.println("Distance: " + wayLength);
                System.out.println("Enemy: " + board.getEnemyHeads().size() +
                        ", common length: " + board.getEnemyBodies().size());
                System.out.println("My length: " + board.getMyBody().size());
                Direction curDirection = Direction.STOP;
                BoardPoint tmpPoint = new BoardPoint(maxIndex % size, maxIndex / size);
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

    private static int getWeight(BoardPoint point, Direction direction, int wayLength, GameBoard board) {
        int weight = 0;
        weight = evilCount - 1 > wayLength ?
                board.getElementAt(point).getEvilWeight() :
                board.getElementAt(point).getWeight();
        if (wayLength == 0 && (prevDirection == Direction.RIGHT && direction == Direction.LEFT ||
                prevDirection == Direction.LEFT && direction == Direction.RIGHT ||
                prevDirection == Direction.UP && direction == Direction.DOWN ||
                prevDirection == Direction.DOWN && direction == Direction.UP))
            weight = BAD_WEIGHT;
        if (board.getEnemyHeads().contains(point)) {
            if (evilCount - 1 > wayLength && !board.hasElementAt(point, ENEMY_HEAD_EVIL) ||
                    board.getEnemyBodies().size() / board.getEnemyHeads().size() <
                            board.getMyBody().size() + 2 + board.getEnemyHeads().size())
                weight = 30 - wayLength + evilCount;
            else
                weight = BAD_WEIGHT;
        }
        if (board.hasElementAt(point, FURY_PILL))
            weight -= evilCount;
        return weight == 0 || weight == BAD_WEIGHT ? weight : weight - wayLength;
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
                if (board.hasElementAt(new BoardPoint(i, j), WALL)) s = "#";
                if (board.hasElementAt(new BoardPoint(i, j), STONE)) s = "o";
                System.out.print(s + " ");
            }
            System.out.println();
        }
    }
}
