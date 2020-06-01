package ru.codebattle.client.api;

import java.util.*;

import lombok.Getter;

import static ru.codebattle.client.api.BoardElementWithWeight.*;

public class GameBoard {

    public GameBoard(String boardString) {
        this.boardString = boardString.replace("\n", "");
    }

    @Getter
    private String boardString;

    public int size() {
        return (int) Math.sqrt(boardString.length());
    }

    public BoardPoint getMyHead() {
        return findFirstElement(HEAD_DEAD, HEAD_DOWN, HEAD_UP, HEAD_LEFT, HEAD_RIGHT, HEAD_EVIL,
                HEAD_FLY, HEAD_SLEEP);
    }

    public List<BoardPoint> getWalls() {
        return findAllElements(WALL);
    }

    public List<BoardPoint> getStones() {
        return findAllElements(STONE);
    }

    public int getMyLength() {
        return 2 + getMyBody().size();
    }

    public List<BoardPoint> getApples() {
        return findAllElements(APPLE);
    }

    public boolean amIEvil() {
        return findAllElements(HEAD_EVIL).contains(getMyHead());
    }

    public boolean amIFlying() {
        return findAllElements(HEAD_FLY).contains(getMyHead());
    }

    public List<BoardPoint> getFlyingPills() {
        return findAllElements(FLYING_PILL);
    }

    public List<BoardPoint> getFuryPills() {
        return findAllElements(FURY_PILL);
    }

    public List<BoardPoint> getGold() {
        return findAllElements(GOLD);
    }

    public List<BoardPoint> getStartPoints() {
        return findAllElements(START_FLOOR);
    }

    public List<BoardPoint> getBarriers() {
        return findAllElements(WALL, START_FLOOR, ENEMY_HEAD_SLEEP, ENEMY_TAIL_INACTIVE, TAIL_INACTIVE, STONE,
                ENEMY_BODY_HORIZONTAL, ENEMY_BODY_LEFT_DOWN, ENEMY_BODY_LEFT_UP, ENEMY_BODY_RIGHT_DOWN,
                ENEMY_BODY_RIGHT_UP, ENEMY_BODY_VERTICAL, ENEMY_HEAD_EVIL, ENEMY_HEAD_DOWN, ENEMY_HEAD_LEFT,
                ENEMY_HEAD_RIGHT, ENEMY_HEAD_UP);
    }

    public List<BoardPoint> getBarriersEvil() {
        return findAllElements(WALL, START_FLOOR, ENEMY_HEAD_SLEEP);
    }

    public List<BoardPoint> getBounty() {
        return findAllElements(APPLE, GOLD, FURY_PILL, FLYING_PILL);
    }

    public List<BoardPoint> getBountyEvil() {
        return findAllElements(APPLE, GOLD, FURY_PILL, FLYING_PILL,
                ENEMY_BODY_HORIZONTAL, ENEMY_BODY_LEFT_DOWN, ENEMY_BODY_LEFT_UP, ENEMY_BODY_RIGHT_DOWN,
                ENEMY_BODY_RIGHT_UP, ENEMY_BODY_VERTICAL, ENEMY_HEAD_DOWN, ENEMY_HEAD_LEFT,
                ENEMY_HEAD_RIGHT, ENEMY_HEAD_UP);
    }

    public List<BoardPoint> getMyBody() {
        return findAllElements(BODY_HORIZONTAL, BODY_LEFT_DOWN, BODY_LEFT_UP, BODY_RIGHT_DOWN,
                BODY_RIGHT_UP, BODY_VERTICAL, STONE);
    }

    public BoardPoint getMyTail() {
        return findFirstElement(TAIL_END_DOWN, TAIL_END_LEFT, TAIL_END_RIGHT, TAIL_END_UP, TAIL_INACTIVE);
    }

    public boolean hasElementAt(BoardPoint point, BoardElementWithWeight element) {
        if (point.isOutOfBoard(size()))
            return false;
        return getElementAt(point) == element;
    }

    public BoardElementWithWeight getElementAt(BoardPoint point) {
        return BoardElementWithWeight.valueOf(boardString.charAt(getShiftByPoint(point)));
    }

    public void printBoard() {
        for (int i = 0; i < size(); i++)
            System.out.println(boardString.substring(i * size(), size() * (i + 1)));
    }

    public BoardPoint findElement(BoardElementWithWeight elementType) {
        for (int i = 0; i < size() * size(); i++) {
            BoardPoint pt = getPointByShift(i);
            if (hasElementAt(pt, elementType))
                return pt;
        }
        return null;
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

    public void boardPreProcess() {
        // TODO подготовка карты
        StringBuilder newBoard = new StringBuilder(boardString);
        for (int i = 0; i < boardString.length(); i++) {
            BoardPoint point = getPointByShift(i);
            ArrayList<BoardElementWithWeight> constantElement = new ArrayList<>(Arrays.asList(WALL, START_FLOOR,
                    OTHER, HEAD_DEAD, HEAD_DOWN, HEAD_EVIL, HEAD_FLY, HEAD_LEFT, HEAD_RIGHT, HEAD_UP,
                    HEAD_SLEEP, ENEMY_HEAD_DEAD, ENEMY_HEAD_SLEEP));
            if (!constantElement.contains(getElementAt(point))) {
                if (hasElementAt(point, ENEMY_HEAD_EVIL, ENEMY_HEAD_FLY, ENEMY_HEAD_UP,
                        ENEMY_HEAD_RIGHT, ENEMY_HEAD_DOWN, ENEMY_HEAD_LEFT)) {
                    Set<BoardPoint> fourPoint = new HashSet<>(Arrays.asList(point.shiftBottom(),
                            point.shiftLeft(), point.shiftRight(), point.shiftTop()));
                    for (BoardPoint neiPoint : fourPoint) {
                        if (!constantElement.contains(getElementAt(neiPoint)))
                            newBoard.replace(getShiftByPoint(neiPoint), getShiftByPoint(neiPoint) + 1,
                                    String.valueOf(getElementAt(point)));
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
                        newBoard.replace(getShiftByPoint(point), getShiftByPoint(point) + 1,
                                String.valueOf(WALL.symbol));
                    else if (wallCount + stoneCount >= 3)
                        newBoard.replace(getShiftByPoint(point), getShiftByPoint(point) + 1,
                                String.valueOf(STONE.symbol));

                }
            }
        }
        boardString = new String(newBoard);
    }

    public boolean hasElementAt(BoardPoint point, BoardElementWithWeight... elements) {
        return Arrays.stream(elements).anyMatch(element -> hasElementAt(point, element));
    }

    private int getShiftByPoint(BoardPoint point) {
        return point.getY() * size() + point.getX();
    }

    private BoardPoint getPointByShift(int shift) {
        return new BoardPoint(shift % size(), shift / size());
    }
}
