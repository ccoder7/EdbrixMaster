package com.edbrix.contentbrix.data;

import java.io.Serializable;

/**
 * Created by rajk on 11/12/17.
 */

public class BoardData implements Serializable {
    private String id;
    private String boardName;

    public static BoardData addBoardData(String id, String boardName) {
        return new BoardData(id, boardName);
    }


    public BoardData(String id, String boardName) {
        this.id = id;
        this.boardName = boardName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }
}
