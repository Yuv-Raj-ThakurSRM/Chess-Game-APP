# **Chess Game System**

## **Project Overview**
The Chess Game System is an interactive, fully functional chess application developed in Java. It provides both Player vs Player and Player vs Computer gameplay with varying AI difficulty levels. Designed to be modular and extensible, the project includes move validation, rule enforcement, and AI based on Minimax and Alpha-Beta Pruning algorithms.

## **Features**
- **Player vs Player mode**
- **Player vs Computer mode** (AI: Easy, Medium, Hard)
- **Move validation and rule enforcement**
- **Check, checkmate, and stalemate detection**
- **Undo and restart options**
- **Colorful, user-friendly GUI** (Java Swing, optional JavaFX)
- **Modular code for scalability**
- **Save/Load and replay features** (future enhancement)
- **AI based on Minimax and Alpha-Beta Pruning algorithms**

## **Requirements**
- **Programming Language:** Java (JDK 1.8+)
- **IDE:** Eclipse, IntelliJ IDEA, NetBeans
- **GUI Framework:** Java Swing/JavaFX
- **AI:** Minimax, Alpha-Beta Pruning
- **Database:** MySQL/SQLite (for multiplayer, future)
- **OS:** Windows, Linux, macOS

## **Usage Instructions**
1. Open the project in your preferred Java IDE.
2. Build the project using Java JDK 1.8 or later.
3. Run the main class to launch the GUI.
4. Select either **PvP** or **PvC** mode, choose AI difficulty (if PvC).
5. Play the game using GUI controls.
6. Use **Restart** or **Undo** buttons for enhanced gameplay flexibility.

## **Architecture**
- **Presentation Layer:** GUI (Swing/JavaFX)
- **Business Logic Layer:** Game engine, AI logic
- **Data Layer:** Database support for saved games/multiplayer (optional)
- **Modular Structure:** Each chess piece is a class inheriting from a common superclass for clean code and polymorphic behavior.

## **Development Timeline (Example)**
- **Week 1-2:** Setup & GUI design  
- **Week 3-4:** Move validation  
- **Week 5-6:** PvP gameplay  
- **Week 7-8:** AI development  
- **Week 9-10:** Undo/Restart, testing  
- **Week 11-12:** Save/Load, multiplayer (optional)

## **Future Enhancements**
<details>
  <summary><strong>Click to expand — Planned Enhancements</strong></summary>

- Online multiplayer  
- Mobile application  
- Integration with advanced chess engines (Stockfish)  
- Training/hint mode  
- Machine learning AI

</details>

## **Credits**
**Developed by:** Yuv Raj Singh Thakur, Laksh Nagori, Akshat Singh, Asif Hammad.
