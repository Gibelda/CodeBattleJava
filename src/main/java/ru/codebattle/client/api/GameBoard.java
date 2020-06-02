package ru.codebattle.client.api;

import java.util.*;

import lombok.Getter;

import static ru.codebattle.client.api.BoardElementWithWeight.*;

public class GameBoard {

    public GameBoard(String boardString) {
        this.rawBoardString = boardString;
        this.boardString = new StringBuilder(boardString.replace("\n", ""));
        this.myHead = findFirstElement(HEAD_DEAD, HEAD_DOWN, HEAD_UP, HEAD_LEFT, HEAD_RIGHT, HEAD_EVIL,
                HEAD_FLY, HEAD_SLEEP);
        this.myBody = findAllElements(BODY_HORIZONTAL, BODY_VERTICAL, BODY_LEFT_DOWN, BODY_LEFT_UP,
                BODY_RIGHT_DOWN, BODY_RIGHT_UP);
        this.myTail = findFirstElement(TAIL_END_DOWN, TAIL_END_LEFT, TAIL_END_RIGHT, TAIL_END_UP, TAIL_INACTIVE);
        this.enemyHeads = findAllElements(ENEMY_HEAD_DOWN, ENEMY_HEAD_LEFT, ENEMY_HEAD_RIGHT, ENEMY_HEAD_UP,
                ENEMY_HEAD_DEAD, ENEMY_HEAD_EVIL, ENEMY_HEAD_FLY, ENEMY_HEAD_SLEEP);
        this.enemyBodies =  findAllElements(ENEMY_BODY_HORIZONTAL, ENEMY_BODY_VERTICAL, ENEMY_BODY_LEFT_DOWN, ENEMY_BODY_LEFT_UP,
                ENEMY_BODY_RIGHT_DOWN, ENEMY_BODY_RIGHT_UP);
        this.enemyTails = findAllElements(ENEMY_TAIL_END_DOWN, ENEMY_TAIL_END_LEFT, ENEMY_TAIL_END_UP,
                ENEMY_TAIL_END_RIGHT, ENEMY_TAIL_INACTIVE);
    }

    @Getter private StringBuilder boardString;
    @Getter private String rawBoardString;
    @Getter private BoardPoint myHead;
    @Getter private List<BoardPoint> myBody;
    @Getter private BoardPoint myTail;
    @Getter private List<BoardPoint> enemyHeads;
    @Getter private List<BoardPoint> enemyBodies;
    @Getter private List<BoardPoint> enemyTails;

    public int size() {
        return (int) Math.sqrt(boardString.length());
    }

    public boolean hasElementAt(BoardPoint point, BoardElementWithWeight element) {
        if (point.isOutOfBoard(size()))
            return false;
        return getElementAt(point) == element;
    }

    public boolean hasElementAtRaw(BoardPoint point, BoardElementWithWeight element) {
        if (point.isOutOfBoard(size()))
            return false;
        return getElementAtRaw(point) == element;
    }

    public BoardElementWithWeight getElementAt(BoardPoint point) {
        return BoardElementWithWeight.valueOf(boardString.charAt(getShiftByPoint(point)));
    }

    public BoardElementWithWeight getElementAtRaw(BoardPoint point) {
        return BoardElementWithWeight.valueOf(rawBoardString.charAt(getShiftByPoint(point)));
    }

    public BoardPoint findFirstElement(BoardElementWithWeight... elementType) {
        for (int i = 0; i < size() * size(); i++) {
            BoardPoint pt = getPointByShift(i);
            for (BoardElementWithWeight elemType : elementType)
                if (hasElementAt(pt, elemType))
                    return pt;
        }
        return null;
    }

    public List<BoardPoint> findAllElements(BoardElementWithWeight... elementType) {
        List<BoardPoint> result = new ArrayList<>();
        for (int i = 0; i < size() * size(); i++) {
            BoardPoint pt = getPointByShift(i);
            for (BoardElementWithWeight elemType : elementType)
                if (hasElementAt(pt, elemType))
                    result.add(pt);
        }
        return result;
    }

    public boolean hasElementAt(BoardPoint point, BoardElementWithWeight... elements) {
        return Arrays.stream(elements).anyMatch(element -> hasElementAt(point, element));
    }

    public boolean hasElementAtRaw(BoardPoint point, BoardElementWithWeight... elements) {
        return Arrays.stream(elements).anyMatch(element -> hasElementAtRaw(point, element));
    }

    public void boardPreProcess() {
        for (int i = 0; i < boardString.length(); i++) {
            BoardPoint point = getPointByShift(i);
            ArrayList<BoardElementWithWeight> constantElement = new ArrayList<>(Arrays.asList(WALL, START_FLOOR,
                    OTHER, HEAD_DEAD, HEAD_DOWN, HEAD_EVIL, HEAD_FLY, HEAD_LEFT, HEAD_RIGHT, HEAD_UP,
                    HEAD_SLEEP, ENEMY_HEAD_DEAD, ENEMY_HEAD_SLEEP));
            if (!constantElement.contains(getElementAt(point))) {
                if (hasElementAtRaw(point, ENEMY_HEAD_EVIL, ENEMY_HEAD_FLY, ENEMY_HEAD_UP,
                        ENEMY_HEAD_RIGHT, ENEMY_HEAD_DOWN, ENEMY_HEAD_LEFT)) {
                    Set<BoardPoint> fourPoint = new HashSet<>(Arrays.asList(point.shiftBottom(),
                            point.shiftLeft(), point.shiftRight(), point.shiftTop()));
                    for (BoardPoint neiPoint : fourPoint) {
                        if (!constantElement.contains(getElementAt(neiPoint)))
                            boardString.replace(getShiftByPoint(neiPoint), getShiftByPoint(neiPoint) + 1,
                                    String.valueOf(getElementAtRaw(point)));
                    }
                } else {
                    int wallCount = 0;
                    int stoneCount = 0;
                    Set<BoardPoint> fourPoint = new HashSet<>(Arrays.asList(point.shiftBottom(),
                            point.shiftLeft(), point.shiftRight(), point.shiftTop()));
                    for (BoardPoint neiPoint : fourPoint) {
                        if (getElementAt(neiPoint) == WALL)
                            wallCount++;
                        else if (getElementAt(neiPoint) == STONE)
                            stoneCount++;
                    }
                    if (wallCount >= 3)
                        boardString.replace(getShiftByPoint(point), getShiftByPoint(point) + 1,
                                String.valueOf(WALL.symbol));
                    else if (wallCount + stoneCount >= 3)
                        boardString.replace(getShiftByPoint(point), getShiftByPoint(point) + 1,
                                String.valueOf(STONE.symbol));
                }
            }
        }
    }

    private int getShiftByPoint(BoardPoint point) {
        return point.getY() * size() + point.getX();
    }

    private BoardPoint getPointByShift(int shift) {
        return new BoardPoint(shift % size(), shift / size());
    }
}
