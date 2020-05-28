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
        // TODO � ��� ������ ���������?
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
        // ���������� �����������
        Direction curDirection = prevDirection;
        System.out.println(head + " " + prevState + " " + curState + " " + evilCount);
        prevState = curState;
        // ��������� ������� � ��������� ��� ��������� ������ ����
        int size = board.size(); // ������ ������� �����, ������ ����� ������� size^2
        Set<BoardPoint> border = new HashSet<>(); // ��������� ����� ������� �������
        border.add(head);
        Direction[] ways = new Direction[size * size]; // ������ ������ � ��� �����
        int[] weights = boardWeight(board, curState); // ��� ���� �� ���� �����
        boolean[] visits = new boolean[size * size]; // �������� �� ��� �����
        List<BoardPoint> barriers = board.getBarriers(); // ���� ����������� ��� USUAL
        barriers.add(head);
        List<BoardPoint> myBody = board.getMyBody();
        barriers.addAll(myBody);
        if (board.getMyLength() < 4)
            barriers.addAll(board.getMyTail());
        List<BoardPoint> barriersEvil = board.getBarriersEvil(); // ���� ����������� ��� EVIL
        barriers.add(head);
        barriersEvil.addAll(myBody);
        if (board.getMyLength() < 4)
            barriersEvil.addAll(board.getMyTail());
        List<BoardPoint> bounty = board.getBounty(); // ���� ������ ��� USUAL
        List<BoardPoint> bountyEvil = board.getBountyEvil(); // ���� ������ ��� EVIL
        int wayLength = 0;
        while (!border.isEmpty()) { // ���� ������� �������
            HashSet<BoardPoint> neighbors = new HashSet<>(); // ������� ��������� ����� - ����� �������
            for (BoardPoint point : border) { // ��� ������ ����� ������ ������� ������� ���� �� �������
                Map<BoardPoint, Direction> fourPoints = new HashMap<>(); // ������ ����� ������ �����
                fourPoints.put(point.shiftRight(), Direction.RIGHT);
                fourPoints.put(point.shiftBottom(), Direction.DOWN);
                fourPoints.put(point.shiftLeft(), Direction.LEFT);
                fourPoints.put(point.shiftTop(), Direction.UP);
                for (BoardPoint newPoint : fourPoints.keySet()) { // ��� ���� �������
                    int index = newPoint.getY() * size + newPoint.getX(); // ������ �� �����
                    if (newPoint.getX() > 0 && newPoint.getY() > 0 &&
                            newPoint.getX() < size && newPoint.getY() < size &&
                            !visits[index]) { // ������ ���� ��� ����� ��� �� ��������
                        if (curState == State.USUAL && barriers.contains(newPoint) ||
                                curState == State.EVIL && barriersEvil.contains(newPoint)) {
                            // ���� ��� ������, ��
                            weights[index] = Integer.MIN_VALUE; // ��� � ��� �������������
                            ways[index] = null; // ���� � ��� ���
                            visits[index] = true;
                        } else if (curState == State.USUAL && bounty.contains(newPoint) ||
                                curState == State.EVIL && bountyEvil.contains(newPoint)) {
                            if (weights[index] <= weights[index] + weights[point.getY() * size + point.getX()]) {
                                // <= � �� <, ���� ����� �� ���� ��������, �� ����� ���� � ������, � ����� 0
                                weights[index] = weights[index] + weights[point.getY() * size + point.getX()];
                                ways[index] = fourPoints.get(newPoint);
                                neighbors.add(point);
                            }
                            // TODO ��� ���� ���, �� ����: �������� ����
                            // � ����� ��� � �� ����
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
                            // ���� ��� �����
                            weights[index] = weights[point.getY() * size + point.getX()];
                            ways[index] = fourPoints.get(newPoint); // ������ ������
                            neighbors.add(newPoint);
                        }
                    }
                }
            }
            // ��� ��������, ��� ��� ����� ����� ������� ��������
            for (BoardPoint newPoint : neighbors) {
                int index = newPoint.getY() * size + newPoint.getX(); // ������ �� �����
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
