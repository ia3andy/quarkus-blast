package model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.List;

@Entity
public class ScoreEntity extends PanacheEntity {

    @ManyToOne
    public BoardEntity board;

    @ManyToOne
    public User user;

    public int score;

    @CreationTimestamp
    public Timestamp created;

    public static List<ScoreEntity> boardScores(BoardEntity board) {
        return find("board = ?1", Sort.by("score").descending(), board).list();
    }

}