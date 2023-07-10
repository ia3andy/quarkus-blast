package model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Entity
@Table( name = "score",
        uniqueConstraints = { @UniqueConstraint( columnNames = { "board_id", "user_id" } ) } )
public class ScoreEntity extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "board_id")
    public BoardEntity board;

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User user;

    public int score;

    @CreationTimestamp
    public Timestamp created;

    public static List<ScoreEntity> boardScores(BoardEntity board) {
        return find("board = ?1", Sort.by("score").descending(), board).list();
    }

    public static ScoreEntity findUserScore(User user, BoardEntity board) {
        return find("user = ?1 AND board = ?2", user, board).firstResult();
    }

}