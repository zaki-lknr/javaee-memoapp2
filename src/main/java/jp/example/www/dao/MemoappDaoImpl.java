package jp.example.www.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.sql.DataSource;

import jp.example.www.MemoBean;

public class MemoappDaoImpl implements MemoappDao {

    private DataSource getDataSource() {
        Context initContext;
        DataSource ds = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            initContext = new InitialContext();
            try {
                ds = (DataSource) initContext.lookup("jdbc/memoapp_db");
                System.out.println("jdbc/memoapp_db: " + ds);
            } catch (NameNotFoundException e) {
                //e.printStackTrace();
                ds = (DataSource) initContext.lookup("java:comp/env/jdbc/memoapp_db");
                System.out.println("java:/comp/env: " + ds);
            }
        } catch (NamingException | ClassNotFoundException e) {
            System.out.println("exception!!!!");
            //e1.printStackTrace();
            throw new RuntimeException("lookup datasource failed: ", e);
        }

        return ds;
    }

    @Override
    public List<MemoBean> getMemos() {
        Connection con = null;
        Statement smt = null;
        ArrayList<MemoBean> memo_list = new ArrayList<>();

        try {
            DataSource ds = getDataSource();
            con = ds.getConnection();
            System.out.println("con: " + con);
            smt = con.createStatement();

            String select_memo = "select title, memo, modified_date from memo_data;";

            ResultSet result = smt.executeQuery(select_memo);
            while (result.next()) {
                MemoBean memoBean = new MemoBean();
                System.out.println("title: " + result.getString("title"));
                System.out.println("memo: " + result.getString("memo"));
                System.out.println("modify: " + result.getString("modified_date"));
                memoBean.setTitle(result.getString("title"));
                memoBean.setMemo(result.getString("memo"));
                memoBean.setModify(result.getString("modified_date"));
                System.out.println("memobean: " + memoBean);
                memo_list.add(memoBean);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return memo_list;
    }

    @Override
    public void save(MemoBean memo) {
        // -- ここにDBへ保存処理 --
        Connection con = null;
        Statement smt = null;

        try {
            DataSource ds = getDataSource();
            con = ds.getConnection();
            smt = con.createStatement();
            System.out.println("smt: " + smt);

            String create_table = "create table if not exists memo_data (" +
                    "memo_id INT(11) auto_increment not null comment 'ID'," +
                    "category INT(11) comment 'カテゴリ'," +
                    "title VARCHAR(64) comment 'タイトル'," +
                    "memo TEXT comment 'メモ'," +
                    "create_date DATETIME comment '作成日'," +
                    "modified_date DATETIME comment '更新日'," +
                    "primary key (memo_id)" + ") " +
                    "engine=InnoDB default charset=utf8";
            // create table
            smt.executeUpdate(create_table);

            System.out.println("title: " + memo.getTitle());
            System.out.println("text: " + memo.getMemo());
            String insert_memo = "insert into memo_data (" +
                    "category, title, memo, create_date, modified_date" +
                    ") values (" +
                    "0," +
                    "'" + memo.getTitle() + "'," +
                    "'" + memo.getMemo() + "'," +
                    "cast(now() as datetime)," +
                    "cast(now() as datetime) " +
                    ");";
            System.out.println("sql: " + insert_memo);
            smt.executeUpdate(insert_memo);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // -- ここまでDB処理 --

    }

}
