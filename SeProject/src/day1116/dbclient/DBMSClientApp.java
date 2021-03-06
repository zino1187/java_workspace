/*
 *DBeaver 수준은 아니어도, 딕셔너리를 학습하기 위해 데이터베이스 접속 클라이언트를 자바로 만들어본다 
   실무에서는,  SQLPlus를 잘 사용하지 않음..이유) 업무효율성이 떨어지기 때문임 
   				그럼 언제쓰나? 실무현장에서는 개발자의 pc 에는 각종 개발툴들이 있지만, 실제적인 운영 서버에는 
   				보안상 아무것도 설치해서는 안된다. 따라서 서버에는 툴들이 없기때문에 만일 오라클을 유지보수하러 파견을
   				나간 경우, 콘솔창 기반으로 쿼리를 다뤄야할 경우가 종종 있다..이때 SQLPlus 를 써야함..
   				아셧쬬?
개발자들이 개발할때 데이터베이스 다루는 툴을 "데이터베이스 접속 클라이언트"라고 부른다
이러한 툴 들중 꽤 유명한 제품은 Toad, 등이 있다..(유료)    				
Toad는  DBeaver에 비해 기능이 막강하지만 유료이기에, 우리는 DBeaver 를 사용하고 있음    				
 * */
package day1116.dbclient;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class DBMSClientApp extends JFrame{
	JPanel p_west; //서쪽 영역 패널 
	Choice ch_users; //유저명이 출력될 초이스 컴포넌트 
	JPasswordField t_pass;//비밀번호 텍스트 필드 
	JButton bt_login; //접속 버튼
	
	JPanel p_center;//그리드가 적용될 센터패널
	JPanel p_upper; //테이블과 시퀀스를 포함할 패널(그리드 레이아웃 예정)
	JTable t_tables;//유저의 테이블 정보를 출력할 JTable
	JTable t_seq;//유저의 시퀀스 정보를 출력할 JTable
	
	JScrollPane s1,s2;//스크롤  2개 준비
	
	String driver="oracle.jdbc.driver.OracleDriver";
	String url="jdbc:oracle:thin:@localhost:1521:XE";
	String user="system";
	String password="1234";
	Connection con;
	
	//테이블 모델로 가면 여러분들이 피곤하므로, 그냥 이차원벡터로 가겠습니다.괜찬죠? 옛날기술이라면서 무시하시기 
	//있기? 없기? ㅋㅋ 
	
	//테이블을 출력할 벡터 , 및 컬럼
	Vector tableList = new Vector(); //이 벡터안에는 추후 또다른 벡터가 들어갈 예정임. 즉 이차원배열과 동일함
													//단, 이차원배열보다는 크기가 자유로와서 유연함..코딩하기 편함	
	Vector<String> tableColumn = new Vector<String>(); //컬럼명은 당연히 String이므로..
	
	//시퀀스에 필요한 벡터들
	Vector seqList = new Vector();
	Vector<String> seqColumn = new Vector<String>();
	
	//선택한 테이블에 대한 레코드 출력에 필요한 벡터들 
	Vector recordList = new Vector();//레코드를 담게될 벡터
	Vector productColum = new Vector();//product 테이블에 대한 벡터 ?
	Vector empColum = new Vector();//product 테이블에 대한 벡터 ?
	
	public DBMSClientApp() {
		//생성
		p_west = new JPanel();
		ch_users = new Choice();
		t_pass = new JPasswordField();
		bt_login = new JButton("접속");
		
		p_center= new JPanel();
		p_upper = new JPanel();
		p_center.setLayout(new GridLayout(3, 1)); //3층에 1호수
		p_upper.setLayout(new GridLayout(1, 2)); //1층에 2호수
		

		//컬럼정보 초기화 하기(이러면 안되겠네요,,,생성자로 원상복귀) 
		tableColumn.add("table_name");
		tableColumn.add("tablespace_name");
		t_tables = new JTable(tableList,tableColumn); //여기서 초기벡터값을 넣어주세요, 이 시점엔 아직  
																			//db연동을 안한 상태이므로 사이즈가 0이지만, 
																			//추후 메서드 호출시 벡터의 크기가 변경될것이고, 
																			//JTable 을 새로고침하면 되겟죠?
		//시퀀스의 생성자에 벡터 적용하기
		seqColumn.add("sequence_name");
		seqColumn.add("last_number");
		t_seq = new JTable(seqList, seqColumn);
		s1 = new JScrollPane(t_tables);
		s2 = new JScrollPane(t_seq);
		
		//스타일 
		p_west.setPreferredSize(new Dimension(150, 350));
		ch_users.setPreferredSize(new Dimension(145, 30));
		t_pass.setPreferredSize(new Dimension(145, 30));
		bt_login.setPreferredSize(new Dimension(145, 30));
		
		//조립 
		p_west.add(ch_users);
		p_west.add(t_pass);
		p_west.add(bt_login);
		p_upper.add(s1);
		p_upper.add(s2);
		p_center.add(p_upper);
		
		add(p_west, BorderLayout.WEST);
		add(p_center);
		
		setSize(700,750);
		setVisible(true);
		//setDefaultCloseOperation(EXIT_ON_CLOSE); 
		//프로세스만 종료시켜 버리므로, 오라클, 스트림 닫는 처리를
		//할 기회를 잃어버리게 된다..
		//따라서 윈도우 어댑터 구현하여 닫을게 있다면 닫는처리하자!!
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				disConnect(); //시스템 종료전에 닫을 자원이 있을때 먼저 닫을려고 호출함!!!!
				System.exit(0);
			}
		});
		
		bt_login.addActionListener((e)->{
			login(); //선택한 유저로 로그인시도하기!!
		});
		
		//테이블과 리스너 연결 
		t_tables.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				//선택한 좌표의 테이블명 얻기!!
				
				int row = t_tables.getSelectedRow();//선택한  row구하기 
				int col = t_tables.getSelectedColumn();//선택한 column  구하기
				
				System.out.println(t_tables.getValueAt(row, col));
			}
		});
		
		setLocationRelativeTo(null);
		
		connect();//호출!!
		getUserList(); //유저목록 구해오기
		
	}
	
	//오라클에 접속하기 
	public void connect() {
		try {
			Class.forName(driver); //드라이버 로드 
			con = DriverManager.getConnection(url, user, password); //접속시도
			if(con==null) {
				JOptionPane.showMessageDialog(this, user+"계정의 접속에 실패하였습니다.");
			}else {
				this.setTitle(user+" 계정으로 접속 중..."); //프레임 제목에 성공 출력 
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//유저목록 가져오기
	public void getUserList() {
		//pstmt와 result 은 소모품이므로 매 쿼리문마다 1개씩 대응되요
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		
		String sql="select username from dba_users order by username asc";
		
		try {
			pstmt=con.prepareStatement(sql);//쿼리문 준비하기
			rs = pstmt.executeQuery();//반환형을 먼저 적으면 이클립스가 알맞는 메서드를 찾아줍니다
			
			//이제  rs 에들어있는 유저정보를 Choice에 출력합시다 (여러분들이 해보세요)
			while(rs.next()) {
				ch_users.add(rs.getString("username"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			//자원 닫기 
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
	
	//현재 접속 유저의 테이블목록 가져오기
	public void getTableList() {
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		
		String sql="select table_name , tablespace_name from user_tables";
		try {
			pstmt=con.prepareStatement(sql);//쿼리준비
			rs=pstmt.executeQuery();//쿼리실행 및 결과집합 받기!!
			//평소같았으면, 이차원배열 선언한 후 last(), getRow() , 스크롤옵션 등등 아주 성가셨으나, 벡터를 이용하면
			//그런게 필요없음 
			
			
			while(rs.next()) {
				Vector vec = new Vector(); //tableList벡터에 담겨질 벡터
				vec.add(rs.getString("table_name"));
				vec.add(rs.getString("tablespace_name"));
				
				tableList.add(vec);//멤버변수 벡터에 벡터를 담았으니, 이제 멤버변수 벡터는 이차원벡터가 됨
			}
			//완성된 이차원벡터를 JTable에 반영해야 함, 생성자의 인수로 넣어야 함!! 
			//컬럼 정보는 어떻게 가져올까요?? 2개밖에 없으니 고정하면 되겠죠?
			t_tables.updateUI(); //여기서 new 하지 마세요 그냥 updateUI() 합시다
			//테이블의 레코드와 컬럼갯수 확인 (여전히 0인지 체크) 
			//현재 테이블이 컬럼을 몇개로 인식하고 있는지 조사
			System.out.println("컬럼수는 : "+t_tables.getColumnCount());
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}			
		}
	}
	
	//시퀀스 가져오기 
	public void getSeqList() {
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		
		String sql="select sequence_name, last_number from user_sequences";
		
		try {
			pstmt=con.prepareStatement(sql);//쿼리준비
			rs=pstmt.executeQuery();//쿼리실행
			//rs의 내용을 벡터로 옮기자!!, 즉 이차원벡터로 만들자!!
			while(rs.next()) {
				Vector vec = new Vector(); // 레코드를 담을 벡터준비( 일차원) 
				vec.add(rs.getString("sequence_name"));
				vec.add(rs.getString("last_number"));
				seqList.add(vec);//기존 시퀀스 벡터에 추가해서 이차원벡터로 만들자!!
			}
			t_seq.updateUI();//만들어진 벡터를 테이블에 반영한 결과를 업데이트하자
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}			
			
		}
		
		
	}
	
	
	//로그인 
	public void login() {
		//현재 유지되고 있는 접속 객체인 Connection을 끊고, 다른 유저로 접속을 시도한다!!
		disConnect();//접속끊기
		
		user = ch_users.getSelectedItem();//현재 초이스컴포넌트에 선택된 아이템값!!!
		password = new String(t_pass.getPassword()); //비밀번호 설정 
		
		connect();//접속하기~~ ( 하지만 멤버변수가 현재 .system으로 되어 있으므로 멤버변수를 초이스 값으로 교체
						//해야 한다) 
		getTableList(); //바로 이 시점에 로그인하자마자, 이 사람의 테이블 정보를 보여주는게 좋을거 같아요
		getSeqList();
		
		System.out.println("보유한 테이블 "+tableList.size());//잘 나오는데 뭔가 갱신에 문제가 있어요 
		//오늘여기까지 할테니, 저녁때 복습하시면서 한번 체크해보세요~~~
		t_tables.updateUI();
	}
	
	
	//오라클에 접속끊기
	public void disConnect() {
		if(con!=null) {  //아직 con이 선언된게 없으니 선언해주세요
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} 
		}
	}
	
	public static void main(String[] args) {
		new DBMSClientApp();
	}

}












