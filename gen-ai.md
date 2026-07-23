Motivation:
make something fun
做个游戏吧——经典打飞机

Step:
1. 可行性测试，how hard could it be
(we never develop game with java)
> Q: 用java做简易打飞机游戏
AI output in 1 minute.
PlaneGame.java 204 lines 
all in a file

2. problem: 代码紧耦合
what if we want to update the plane? —— OOP
> Q: 如果我要分成不同模块呢，比如己方飞机可能随着升级有不同的功能，比如敌方飞机可能也有shoot（甚至敌方也有子弹，甚至是扇形的子弹，或者激光）甚至有boss，然后可能以后会加入音乐和图片

a project with file tree below and each file 
```
PlaneGame /
├── core/                   // 核心控制层
│   ├── GamePanel.java      // 绘图容器、主循环 (Timer/Thread)
│   ├── KeyInput.java       // 键盘输入监听器
│   └── SoundManager.java   // [扩展] 音频播放器
├── entity/                 // 游戏实体抽象与具体实现
│   ├── GameObject.java     // 基础父类 (坐标、宽高、存活状态)
│   ├── Bullet.java         // 子弹 (普通/激光)
│   ├── plane/              // 飞机体系
│   │   ├── AirPlane.java   // 飞机抽象基类
│   │   ├── Player.java     // 玩家飞机 (含升级逻辑)
│   │   └── Enemy.java      // 敌机抽象基类/具体实现 (如 Boss)
├── weapon/                 // 武器/弹幕发射发射器 (独立解耦)
│   ├── Weapon.java         // 武器接口
│   ├── SpreadWeapon.java   // 扇形发射器
│   └── LaserWeapon.java    // 激光发射器
└── res/                    // [扩展] 资源加载器 (图片/音效)
```
3. generate a UML for the project (because AI needs specific API 与整体框架)
```
```
some API needs to be changed
for example 
Game Object - update method, AI offer update with no param
but update(delta: double) 模仿 godot 的 _physical_process(delta: double)
simple shoot weapon add (stream parellel)count and maxwidth(because AI does not take 同步发射 into account)

4. split the UML for each step(因为如果一次做太多事情，AI会偷懒，人工也无法审阅了解代码逻辑)
(example: pure vibe coding prompt on Monday)
split the task into 7 step:
1. Bounding Box & Collision Primitives(class GameObject)
2. Player HP Lifecycle & Frame-Independent Movement (class Player)
3. Delta Game Loop & Render Container (Main game loop)
4. Multi-Stream SimpleShot & Delta Cooldown (class Weapon)
5. Delta Enemy Spawning & Collision Resolution (class Enemy)
6. Spread Weapon Trigonometry & Strategy Swap (class SpreadWeapon extends Weapon)
7. Boss Multi-Phase Delta Mechanics & Debug Tools(class BossEnemy)

some problem:
1. AI may slack off (do not test the collide between boss and player)
2. AI is insensitive to visual details (render)
3. AI is insensitive to game numerical values.(boss phase-2)
4. Test logic(当原有测试逻辑与新的应用逻辑产生冲突，比如概率，AI会写死概率)

5. more feature - chat about inspiration
e.x Enemy Spawn:
I’d like to add a new feature: enemies should spawn randomly with randomized weapon attributes (cooldown, attack speed) and health values. Different enemy types should yield different scores upon defeat and have varying spawn probabilities.
There is another feature as well: the boss should only appear after a certain score threshold is reached. When the boss appears, there should be no other enemies, or the spawn rate of other enemies should be extremely low.

u should write test case to test the new features thoroughly
---
I want to add a new feature: the higher the score, the stronger the enemies become (higher HP, more simultaneous bullets, faster movement speed), while also yielding more points (which also means a higher score boost when defeating bosses and higher BOSS_SCORE_INCREMENT as score increase).

有时需要解释prompt的点，因为AI没有想到，比如这里武器的升级，AI没有纠正maxwidth 和 count 的关系
e.x RewardItem:
prompt:
我想加一个新的feature，除了enemy下落的还有奖励的物品，随机刷新，玩家碰撞后可获取其特性，比如HP回复(random with min and max, 随着score增加而增加)，weapon升级（cooldown interval缩短，弹道数量增加，子弹威力增加等）
（这里AI曾偷懒没有写所有Reward Item类别的测试）

