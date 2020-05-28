package ru.codebattle.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import ru.codebattle.client.api.*;

public class Main {

    private static final String SERVER_ADDRESS = "http://codebattle-pro-2020s1.westeurope.cloudapp.azure.com/codenjoy-contest/board/player/d7rl93d0nx24x6zlv5h0?code=8926581544870510245&gameName=snakebattle";

    public static void main(String[] args) throws URISyntaxException, IOException {
        SnakeBattleClient client = new SnakeBattleClient(SERVER_ADDRESS);
        client.run(gameBoard -> {
            long date = new Date().getTime();
            Direction direction = Strategy.chooseRightDirection(gameBoard);
            //System.out.println(new Date().getTime() - date);
            boolean act = Strategy.chooseAct();
            return new SnakeAction(act, direction);
        });
        System.in.read();

        client.initiateExit();
    }
}
