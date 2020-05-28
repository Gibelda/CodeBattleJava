package ru.codebattle.client;

import ru.codebattle.client.api.BoardElementWithWeight;
import ru.codebattle.client.api.BoardPoint;
import ru.codebattle.client.api.Direction;
import ru.codebattle.client.api.GameBoard;

import java.util.*;
import java.util.List;

public class Strategy {
    static int evilCount = 0;
    static Direction prevDirection = Direction.STOP;

    enum State {
        SLEEP, EVIL, USUAL
    }

    static State prevState = State.SLEEP;

    public static boolean chooseAct() {
        // TODO а это вообще актуально?
        return false;
    }

    public static Direction chooseRightDirection(GameBoard board) {
        BoardPoint head = board.getMyHead();
        if (head == null || board.getElementAt(head) == BoardElementWithWeight.HEAD_SLEEP) {
            prevState = State.SLEEP;
            prevDirection = Direction.STOP;
            evilCount = 0;
            return prevDirection;
        }
        State curState = prevState;
        evilCount--;
        if (prevState != State.EVIL && board.getElementAt(head) == BoardElementWithWeight.HEAD_EVIL) {
            curState = State.EVIL;
            evilCount = 10;
        }
        if (board.getElementAt(head) != BoardElementWithWeight.HEAD_EVIL) {
            curState = State.USUAL;
            evilCount = 0;
        }
        // Определяем направление
        Direction curDirection = prevDirection;
        System.out.println(head + " " + prevState + " " + curState + " " + evilCount);
        prevState = curState;
        // Объявляем массивы и множества для алгоритма поиска пути
        int size = board.size(); // размер стороны карты, размер всего массива size^2
        Set<BoardPoint> border = new HashSet<>(); // множество точек текущей границы
        border.add(head);
        Direction[] ways = new Direction[size * size]; // откуда пришли в эту точку
        int[] weights = boardWeight(board, curState); // вес пути до этой точки
        boolean[] visits = new boolean[size * size]; // посещена ли эта точка
        List<BoardPoint> barriers = board.getBarriers(); // лист невозможных для USUAL
        barriers.add(head);
        List<BoardPoint> myBody = board.getMyBody();
        barriers.addAll(myBody);
        if (board.getMyLength() < 4)
            barriers.addAll(board.getMyTail());
        List<BoardPoint> barriersEvil = board.getBarriersEvil(); // лист невозможных для EVIL
        barriers.add(head);
        barriersEvil.addAll(myBody);
        if (board.getMyLength() < 4)
            barriersEvil.addAll(board.getMyTail());
        List<BoardPoint> bounty = board.getBounty(); // лист наград для USUAL
        List<BoardPoint> bountyEvil = board.getBountyEvil(); // лист наград для EVIL
        int wayLength = 0;
        while (!border.isEmpty()) { // пока граница непуста
            HashSet<BoardPoint> neighbors = new HashSet<>(); // создаем множество точек - новая граница
            for (BoardPoint point : border) { // для каждой точки старой границы смотрим всех ее соседей
                Map<BoardPoint, Direction> fourPoints = new HashMap<>(); // четыре точки вокруг нашей
                fourPoints.put(point.shiftRight(), Direction.RIGHT);
                fourPoints.put(point.shiftBottom(), Direction.DOWN);
                fourPoints.put(point.shiftLeft(), Direction.LEFT);
                fourPoints.put(point.shiftTop(), Direction.UP);
                for (BoardPoint newPoint : fourPoints.keySet()) { // для этих четырех
                    int index = newPoint.getY() * size + newPoint.getX(); // индекс на карте
                    if (newPoint.getX() > 0 && newPoint.getY() > 0 &&
                            newPoint.getX() < size && newPoint.getY() < size &&
                            !visits[index]) { // только если эту точку еще не посещали
                        if (curState == State.USUAL && barriers.contains(newPoint) ||
                                curState == State.EVIL && barriersEvil.contains(newPoint)) {
                            // если это барьер, то
                            weights[index] = Integer.MIN_VALUE; // вес у нее отрицательный
                            ways[index] = null; // пути в нее нет
                            visits[index] = true;
                        } else if (curState == State.USUAL && bounty.contains(newPoint) ||
                                curState == State.EVIL && bountyEvil.contains(newPoint)) {
                            if (weights[index] <= weights[index] + weights[point.getY() * size + point.getX()]) {
                                // <= а не <, если точка не была посещена, то может быть и справа, и слева 0
                                weights[index] = weights[index] + weights[point.getY() * size + point.getX()];
                                ways[index] = fourPoints.get(newPoint);
                                neighbors.add(point);
                            }
                            // TODO тут пока так, но надо: обратный путь
                            // а может уже и не надо
                            BoardPoint tmpPoint = point;
                            Direction tmpDir = fourPoints.get(newPoint);
                            while (!tmpPoint.equals(head)) {
                                tmpDir = ways[tmpPoint.getY() * size + tmpPoint.getX()];
                                switch (tmpDir) {
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
                            return tmpDir;
                        } else if (weights[index] <= weights[index] + weights[point.getY() * size + point.getX()]) {
                            // если там пусто
                            weights[index] = weights[point.getY() * size + point.getX()];
                            ways[index] = fourPoints.get(newPoint); // оттуда пришли
                            neighbors.add(newPoint);
                        }
                    }
                }
            }
            // тут отмечаем, что все точки новой границы посещены
            for (BoardPoint newPoint : neighbors) {
                int index = newPoint.getY() * size + newPoint.getX(); // индекс на карте
                visits[index] = true;
            }
            border = neighbors;
            wayLength++;
            if (wayLength > 10) {
                barriers.removeAll(myBody);
                barriersEvil.removeAll(myBody);
            }
        }
        return curDirection;
    }

    private static int[] boardWeight(GameBoard board, State state) {
        int size = board.size();
        int[] weights = new int[size * size];
        for (int i = 0; i < size * size; i++) {
            if (state == State.EVIL)
                weights[i] = board.getElementAt(new BoardPoint(i % size, i / size)).getEvilWeight();
            else
                weights[i] = board.getElementAt(new BoardPoint(i % size, i / size)).getWeight();
        }
        return weights;
    }

    private static void myPrint(int size, Direction[] ways, int index, BoardPoint head) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                String s = "_";
                if (ways[i * size + j] == Direction.UP) s = "^";
                if (ways[i * size + j] == Direction.DOWN) s = "v";
                if (ways[i * size + j] == Direction.LEFT) s = "<";
                if (ways[i * size + j] == Direction.RIGHT) s = ">";
                if (i * size + j == index) s = "$";
                if (i * size + j == head.getY() * size + head.getX()) s = "X";
                System.out.print(s + " ");
            }
            System.out.println();
        }
    }
}
