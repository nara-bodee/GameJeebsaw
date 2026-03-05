# DEV BIRD System Architecture

## System Overview Diagram

```
╔════════════════════════════════════════════════════════════════════════════╗
║                         GAME JEEBSAW ARCHITECTURE                          ║
╚════════════════════════════════════════════════════════════════════════════╝

┌─────────────────────────────────────────────────────────────────────────────┐
│                              NETWORK LAYER                                  │
│  ┌──────────────┐  TCP Socket  ┌──────────────┐  TCP Socket  ┌───────────┐ │
│  │   Client 1   │◄────────────►│  GameServer  │◄────────────►│  Client 2 │ │
│  │  (P1/DEV TAO)│              │   (Port 7777)│              │ (P2/DEV TAO)
│  └──────────────┘              └──────────────┘              └───────────┘ │
│                                       ▲                                      │
│                                       │ TCP Socket                           │
│                                       ▼                                      │
│                                  ┌──────────┐                               │
│                                  │  Client 3│                               │
│                                  │(P3/DEV TAO)                              │
│                                  └──────────┘                               │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                        SERVER ARCHITECTURE                                  │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │ GameServer                                                            │   │
│  │  - Listens on 7777                                                   │   │
│  │  - Accepts client connections                                        │   │
│  │  - Creates ClientHandler per client                                 │   │
│  │  - Manages RoomSession and SessionRegistry                          │   │
│  │                                                                       │   │
│  │  ┌─────────────────────────────────────────────────────────────┐   │   │
│  │  │ RoomSession ("ROOM1")                                        │   │   │
│  │  │                                                              │   │   │
│  │  │  ┌──────────────────────────────────────────────────────┐  │   │   │
│  │  │  │ GameEngine ★ MAIN LOGIC                             │  │   │   │
│  │  │  │                                                       │  │   │   │
│  │  │  │  GameState:                                          │  │   │   │
│  │  │  │  ├─ dayIndex (1-7)                                   │  │   │   │
│  │  │  │  ├─ turnIndex (0-2)                                  │  │   │   │
│  │  │  │  ├─ currentEventId                                   │  │   │   │
│  │  │  │  ├─ timerRemainingSec                                │  │   │   │
│  │  │  │  └─ players[] {love, charm, money, connected}        │  │   │   │
│  │  │  │                                                       │  │   │   │
│  │  │  │  TurnManager:                                         │  │   │   │
│  │  │  │  ├─ currentPlayerId()                                │  │   │   │
│  │  │  │  └─ nextTurn()                                        │  │   │   │
│  │  │  │                                                       │  │   │   │
│  │  │  │  StoryLoader:                                         │  │   │   │
│  │  │  │  ├─ loadEvents() from story.json                     │  │   │   │
│  │  │  │  └─ eventCache: {eventId → Event}                    │  │   │   │
│  │  │  │                                                       │  │   │   │
│  │  │  │  Methods:                                             │  │   │   │
│  │  │  │  ├─ onStartGame()    → Initialize 3 players          │  │   │   │
│  │  │  │  ├─ onPacket()       → Route incoming packets        │  │   │   │
│  │  │  │  ├─ handleChoice()   → Apply effects & advance       │  │   │   │
│  │  │  │  ├─ onDisconnect()   → Mark disconnected, grace     │  │   │   │
│  │  │  │  └─ onReconnect()    → Send STATE_SYNC               │  │   │   │
│  │  │  └──────────────────────────────────────────────────────┘  │   │   │
│  │  │                                                              │   │   │
│  │  │  ClientHandlers:                                            │   │   │
│  │  │  ├─ ClientHandler (P1)  → Socket, read/write, parse       │   │   │
│  │  │  ├─ ClientHandler (P2)  → Forward to GameEngine            │   │   │
│  │  │  └─ ClientHandler (P3)  → Broadcast via ServerBroadcaster │   │   │
│  │  │                                                              │   │   │
│  │  └──────────────────────────────────────────────────────────┘  │   │   │
│  │                                                                  │   │   │
│  │  SessionRegistry: {token → playerId}                           │   │   │
│  │  - Validates reconnection tokens                               │   │   │
│  │  - Maps tokens to player IDs                                   │   │   │
│  │                                                                  │   │   │
│  │  ServerBroadcaster: broadcast(room, packet)                    │   │   │
│  │  - Sends packet to all connected players                       │   │   │
│  └──────────────────────────────────────────────────────────────┘   │   │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                      DATA MODELS (SHARED)                                   │
│                                                                              │
│  Player                  Event                  Choice                      │
│  ├─ playerId (P1..P3)    ├─ day (1-7)          ├─ choiceId               │
│  ├─ name                 ├─ eventId            ├─ text                    │
│  ├─ love (stat)          ├─ background         ├─ require                 │
│  ├─ charm (stat)         ├─ heroineSprite      │  ├─ charmMin             │
│  ├─ money (stat)         ├─ dialogue[]         │  └─ moneyMin             │
│  └─ connected (bool)     └─ choices[]          ├─ effect                  │
│                                                 │  ├─ love: int            │
│                          Effect                │  ├─ charm: int           │
│                          ├─ love: int          │  └─ money: int           │
│                          ├─ charm: int         └─ resultText              │
│                          └─ money: int                                     │
│                                                                              │
│  Requirement             DataPacket           PacketType                   │
│  ├─ charmMin             ├─ type               HELLO                       │
│  └─ moneyMin             ├─ roomId             RECONNECT                   │
│                          ├─ playerId           STATE_SYNC                  │
│                          ├─ token              CHOICE_SUBMIT               │
│                          ├─ ts                 STATE_UPDATE                │
│                          └─ payload {}         TURN_START                  │
│                                                GAME_OVER                   │
│                                                PLAYER_DISCONNECTED         │
│                                                PLAYER_RECONNECTED          │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                       STORY DATA (JSON)                                     │
│                                                                              │
│  resources/story.json                                                      │
│  {                                                                           │
│    "days": [                                                                │
│      {                                                                      │
│        "day": 1,                                                            │
│        "eventId": "D1_E1",                                                  │
│        "background": "path/to/image.png",                                   │
│        "heroineSprite": "path/to/sprite.png",                              │
│        "dialogue": ["Line 1", "Line 2"],                                    │
│        "choices": [                                                         │
│          {                                                                  │
│            "choiceId": "C1_1",                                              │
│            "text": "Choice text",                                           │
│            "require": {"charmMin": 0, "moneyMin": 50},                     │
│            "effect": {"love": 20, "charm": 5, "money": -50},               │
│            "resultText": "Response from heroine"                            │
│          }                                                                  │
│        ]                                                                    │
│      }, ... Day 2-7 ...                                                    │
│    ]                                                                        │
│  }                                                                           │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Game State Flow Diagram

```
┌─────────────┐
│   CONNECT   │
│   3 Players │
└──────┬──────┘
       │
       ▼
  ┌─────────────────────┐
  │   START_GAME        │
  │ Initialize Game     │
  │ day=1, turn=0, P1   │
  │ players[] initialized
  └──────┬──────────────┘
         │
    ┌────▼────────────────────────────────────┐
    │  TURN_START (Broadcast to all)           │
    │  {currentTurnPlayerId: "P1",             │
    │   timerSec: 15}                          │
    └────┬─────────────────────────────────────┘
         │
    ┌────▼──────────────────────────────────────┐
    │  CHOICE_SUBMIT (P1 submits choice)         │
    │  {eventId: "D1_E1", choiceId: "C1_1"}     │
    └────┬───────────────────────────────────────┘
         │
    ┌────▼────────────────────────────────────────┐
    │  APPLY EFFECT                              │
    │  P1.love += 20                             │
    │  P1.charm += 5                             │
    └────┬─────────────────────────────────────────┘
         │
    ┌────▼──────────────────────────────────────┐
    │  ADVANCE TURN                             │
    │  turnIndex = (0 + 1) % 3 = 1 (P2's turn) │
    │  currentEventId = "D1_E1"                 │
    └────┬───────────────────────────────────────┘
         │
    ┌────▼──────────────────────────────────┐
    │  STATE_UPDATE (Broadcast state)       │
    │  {day: 1, turn: 1, eventId: "D1_E1", │
    │   players: [P1 updated, P2, P3]}      │
    └────┬───────────────────────────────────┘
         │
    ┌────▼──────────────────────────────────┐
    │  TURN_START (P2's turn)               │
    │  {currentTurnPlayerId: "P2",          │
    │   timerSec: 15}                       │
    └────┬───────────────────────────────────┘
         │
         │ ← [Repeat for P2, P3]
         │    [22 more times for days 2-7]
         │
    ┌────▼──────────────────────────────────┐
    │  DAY ADVANCEMENT                      │
    │  After P3's turn on day 1:            │
    │  turnIndex = 0 → dayIndex = 2         │
    │  currentEventId = "D2_E1"             │
    └────┬───────────────────────────────────┘
         │
         │ ← [Repeat for days 2-7]
         │
    ┌────▼──────────────────────────────────┐
    │  GAME END (P3's turn on day 7)        │
    │  dayIndex becomes 8 → Game Over       │
    │  Calculate winner (max love)          │
    └────┬───────────────────────────────────┘
         │
    ┌────▼──────────────────────────────────┐
    │  GAME_OVER                            │
    │  {winnerPlayerId: "P1"}               │ (if P1 has highest love)
    └──────────────────────────────────────┘
```

---

## Disconnection & Reconnection Flow

```
NORMAL PLAY
    │
    ▼
┌─────────────────────────────────┐
│  Socket closes or read() fails  │
│  (Client disconnects)           │
└────────┬────────────────────────┘
         │
         ▼
    ┌──────────────────────────────────────────┐
    │ onDisconnect(playerId)                   │
    │ ├─ Mark p.connected = false              │
    │ ├─ Start 60-second grace timer           │
    │ └─ disconnectedAt[playerId] = now()      │
    └────┬─────────────────────────────────────┘
         │
         ▼
    ┌──────────────────────────────────────────┐
    │ Broadcast PLAYER_DISCONNECTED            │
    │ {playerId: "P2", graceSec: 60}           │
    │ (Other players see "waiting for P2")     │
    └────┬─────────────────────────────────────┘
         │
         ├─ Game continues (skip P2's turns) ──┐
         │                                       │
         └─ P2 can reconnect within 60 sec ───┐│
                                                ││
    ┌───────────────────────────────────────────┘│
    │ Client sends RECONNECT {token}             │
    │                                            │
    ▼                                            │
┌──────────────────────────────────────────────────┐
│ Server validates token in SessionRegistry       │
│ if token not found → send REJECT                │
│ if token valid → proceed                       │
└─────┬────────────────────────────────────────────┘
      │
      ▼
┌──────────────────────────────────────────────────┐
│ onReconnect(handler)                            │
│ ├─ Mark p.connected = true                      │
│ ├─ Remove from disconnectedAt                   │
│ └─ Prepare full state snapshot                  │
└─────┬────────────────────────────────────────────┘
      │
      ▼
┌──────────────────────────────────────────────────┐
│ Send STATE_SYNC                                  │
│ {fullState: {                                    │
│   day: 3,                                        │
│   turn: 1,                                       │
│   currentEventId: "D3_E1",                       │
│   players: [...]                                 │
│ }}                                               │
└─────┬────────────────────────────────────────────┘
      │
      ▼
┌──────────────────────────────────────────────────┐
│ Broadcast PLAYER_RECONNECTED                     │
│ {playerId: "P2"}                                 │
│ (Other players see "P2 is back")                │
└─────┬────────────────────────────────────────────┘
      │
      ▼
┌──────────────────────────────────────────────────┐
│ Broadcast STATE_UPDATE to all                    │
│ (Sync all clients to same state)                │
└─────┬────────────────────────────────────────────┘
      │
      ▼
┌──────────────────────────────────────────────────┐
│ Game continues normally with all 3 players       │
└──────────────────────────────────────────────────┘
```

---

## Packet Flow Sequence

```
Sequence: New Game with 3 Players

Client1          Client2          Client3          Server
   │                │                │                │
   ├────HELLO───────────────────────────────────────>│
   │                │                │                │ (assign P1, token1)
   │                │                │                │
   │                │                │                ├─register P1
   │<───HELLO_OK────────────────────────────────────┤ (send token1)
   │                │                │                │
   │                │<───HELLO───────────────────────┤
   │                │                │                │ (assign P2, token2)
   │                │                │                │
   │                │                │                ├─register P2
   │                │<───HELLO_OK─────────────────────┤ (send token2)
   │                │                │                │
   │                │                ├────HELLO──────>│
   │                │                │                │ (assign P3, token3)
   │                │                │                │
   │                │                │                ├─register P3
   │                │                │<───HELLO_OK────┤ (send token3)
   │                │                │                │ 3 players connected!
   │                │                │                │
   ├─START_GAME────┴────────────────┴─START_GAME────┤
   │ (both clients)  │                │                │
   │                │                │       ┌─ init GameState
   │                │                │       ├─ day=1, turn=0, P1
   │                │                │       └─ broadcast TURN_START
   │                │                │                │
   │<───TURN_START─────────────────────────────────────┤
   │   P1, 15 sec   │                │                │
   │                │<───TURN_START────────────────────┤
   │                │   P1, 15 sec   │                │
   │                │                │<───TURN_START───┤
   │                │                │   P1, 15 sec    │
   │                │                │                │
   │  (P1 chooses)  │                │                │
   │                │                │       ┌─ apply effect
   ├─CHOICE_SUBMIT─────────────────────────>│ ├─ advance turn
   │C1_1, D1_E1     │                │       │ └─ broadcast updates
   │                │                │       │
   │<───STATE_UPDATE───────────────────────────────────┤
   │{fullState}     │                │                │
   │                │<───STATE_UPDATE───────────────────┤
   │                │{fullState}     │                │
   │                │                │<───STATE_UPDATE─┤
   │                │                │{fullState}      │
   │                │                │                │
   │                │<───TURN_START──────────────────────┤
   │                │   P2, 15 sec                       │
   │                │                                    │
   │  ← [... similar pattern for turns 2-21 ...] ←     │
   │                │                                    │
   │<───GAME_OVER──────────────────────────────────────┤
   │   {winner: P1} │                │                │
   │                │<───GAME_OVER───────────────────────┤
   │                │   {winner: P1} │                │
   │                │                │<───GAME_OVER────┤
   │                │                │   {winner: P1}  │
```

---

## Game Logic Processing Pipeline

```
Client sends CHOICE_SUBMIT
        │
        ▼
ClientHandler receives packet
        │
        ▼
Parse JSON → DataPacket
        │
        ▼
ClientHandler.run() forwards to engine.onPacket()
        │
        ▼
GameEngine.onPacket() routes by type
        │
        ▼
GameEngine.handleChoice()
        │
    ┌───┴─────────────────────────────────┐
    │                                     │
    ▼                                     ▼
Verify it's         Load Choice from
player's turn       StoryLoader cache
    │                                     │
    └───────────┬─────────────────────────┘
                │
                ▼
        Apply Effect to Player:
        ├─ player.love += choice.effect.love
        ├─ player.charm += choice.effect.charm
        └─ player.money += choice.effect.money
                │
                ▼
        Advance Turn:
        turnIndex = (turnIndex + 1) % 3
                │
                ▼
        Check if New Day:
        if turnIndex == 0
        ├─ dayIndex++
        └─ currentEventId = "D" + dayIndex + "_E1"
                │
                ▼
        Check if Game Over:
        if dayIndex > 7
        ├─ endGame()
        └─ broadcast GAME_OVER
                │
                ▼
        Broadcast Updates:
        ├─ broadcastTurnStart()  [TURN_START packet]
        └─ broadcastState()      [STATE_UPDATE packet]
                │
                ▼
        Ready for Next Player
```

---

## Data Serialization Flow

```
GameState Object
        │
        ▼
state.toMap()
{
  "dayIndex": 1,
  "turnIndex": 0,
  "currentEventId": "D1_E1",
  "timerRemainingSec": 15,
  "players": [
    {
      "playerId": "P1",
      "name": "Player 1",
      "love": 20,
      "charm": 5,
      "money": 95,
      "connected": true
    }, {...}, {...}
  ]
}
        │
        ▼
DataPacket.with("fullState", map)
        │
        ▼
PacketCodec.toJson(packet)
        │
        ▼
JSON String (sent over network)
{
  "type": "STATE_UPDATE",
  "roomId": "ROOM1",
  "playerId": null,
  "token": null,
  "ts": 1700000000000,
  "payload": {
    "fullState": {
      "dayIndex": 1,
      "turnIndex": 0,
      "currentEventId": "D1_E1",
      "timerRemainingSec": 15,
      "players": [...]
    }
  }
}
        │
        ▼
Network transmission
        │
        ▼
Client receives JSON
        │
        ▼
PacketCodec.fromJson(json)
        │
        ▼
DataPacket object
        │
        ▼
DEV TAO parses payload
        │
        ▼
Update UI with new state
```

---

## Thread Safety Model

```
GameEngine (Main Thread)
├─ onPacket() ← Called by ClientHandler threads
├─ handleChoice() ← Accesses GameState
├─ onDisconnect() ← Creates new Thread for grace timer
└─ onReconnect() ← Called by ClientHandler thread

GameState (Shared Mutable)
├─ dayIndex (int)
├─ turnIndex (int)
├─ currentEventId (String)
├─ timerRemainingSec (int)
└─ players (ArrayList)
    └─ [Accessed from multiple threads]
    └─ [Should be synchronized in production]

ClientHandler[] (In RoomSession)
├─ handlers = Collections.synchronizedList<>()
└─ Self-synchronized for thread-safe iteration

SessionRegistry
├─ tokenToPlayerId = ConcurrentHashMap<>()
└─ Thread-safe key-value store

disconnectedAt (In GameEngine)
├─ = ConcurrentHashMap<>()
└─ Thread-safe disconnect tracking
```

---

**DEV BIRD System Architecture Complete ✅**

This comprehensive architecture provides the foundation for a production-ready multiplayer game with reconnection support, persistent state management, and story-driven gameplay.

