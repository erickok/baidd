package nl.uu.cs.arg.platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import nl.uu.cs.arg.shared.Agent;
import nl.uu.cs.arg.shared.dialogue.Dialogue;
import nl.uu.cs.arg.shared.dialogue.DialogueException;
import nl.uu.cs.arg.shared.dialogue.DialogueMessage;
import nl.uu.cs.arg.shared.dialogue.DialogueStartedMessage;
import nl.uu.cs.arg.shared.dialogue.DialogueState;
import nl.uu.cs.arg.shared.dialogue.DialogueStateChangeMessage;
import nl.uu.cs.arg.shared.dialogue.Goal;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.OutcomeMessage;
import nl.uu.cs.arg.shared.dialogue.SkipMoveMessage;
import nl.uu.cs.arg.shared.dialogue.locutions.DenyDialogueLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.JoinDialogueLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.shared.dialogue.locutions.OpenDialogueLocution;
import nl.uu.cs.arg.shared.dialogue.protocol.DeliberationRule;
import nl.uu.cs.arg.shared.dialogue.protocol.ProtocolException;
import nl.uu.cs.arg.shared.dialogue.protocol.TerminationMessage;
import nl.uu.cs.arg.shared.dialogue.protocol.TerminationRule;

import org.aspic.inference.Term;

/**
 * The platform manages the running of a single {@link Dialogue} between some
 * {@link Agent}s. The platform should always be initialized first. Start the
 * platform by wrapping it in a Thread and start()ing it. If the platform needs
 * to halt, call requestStop() instead of killing the Thread yourself. To 
 * listen to the {@link PlatformException}s and messages being generated, 
 * attach a {@link PlatformListener}.
 * 
 * Three types of messages are send to agents and platform listeners: when new 
 * moves were made, dialogue messages (such as state changes or on termination)
 * and exceptions. The first two are send to all agents, but exceptions only to
 * a single receiver. Listeners are notified on all message types.
 *   
 * @author erickok
 *
 */
public class Platform implements Runnable {

	/**
	 * Whether the platform was successfully initialized with agents and a new dialogue 
	 */
	private boolean initialized = false;

	/**
	 * By setting this to true while running the platform, it will stop running as soon as possible
	 */
	private boolean paused;
	
	/**
	 * By setting this to true, the platform will stop running after a single 'step' is made
	 */
	private boolean stopAfterEachStep = false;
	
	/**
	 * A list of listeners to generated platform (error) messages
	 */
	private List<PlatformListener> listeners;
	
	/**
	 * The active platform settings
	 */
	private final Settings settings;

	/**
	 * The ongoing deliberation dialogue
	 */
	private Dialogue dialogue;

	/**
	 * All (legal) moves that are made by the agents in the dialogue
	 */
	private List<Move<? extends Locution>> allMoves;

	/**
	 * The agents that are connected to the platform
	 */
	private List<ParticipatingAgent> connectedAgents;

	/**
	 * The agents that have accepted to join the dialogue
	 */
	private List<ParticipatingAgent> joinedAgents;
	
	/**
	 * The agent that was last to make new moves in the dialogue
	 */
	private ParticipatingAgent lastToMove;

	/**
	 * A counter on the number of subsequent turns that the agents did not make any move (skipped)
	 */
	private int skippedTurnsCount;
	
	/**
	 * The mimicked open-dialogue move to send to each agent on asking to join
	 */
	private Move<OpenDialogueLocution> openDialogueMove;

	/**
	 * Whether the platform should start in paused mode (and not start deliberating until it is 
	 * started explicitly)
	 */
	private boolean startPaused = true;
	
	/**
	 * Instantiate the platform with specific platform settings
	 * @param settings The settings to initialize the platform with
	 */
	public Platform(Settings settings) {
		this.settings = settings;
		this.listeners = new ArrayList<PlatformListener>();
		this.joinedAgents = new ArrayList<ParticipatingAgent>();
	}
	
	/**
	 * Initialize the platform, loading the given (already instantiated) 
	 * agents (which overrides all previous agents, the dialogue, etc.)
	 * @param agents The agents to attach to this platform
	 */
	public void init(Term topic, Goal topicGoal, List<Agent> agents) {
		
		this.dialogue = new Dialogue(topic, topicGoal);
		this.allMoves = new ArrayList<Move<? extends Locution>>();
		Move.resetMoveCounter();
		
		// For each agent, create a participant data structure and initialize it
		int participantCount = 0;
		this.connectedAgents = new ArrayList<ParticipatingAgent>();
		for (Agent agent : agents) {
			
			// Create coupling
			ParticipatingAgent pa = ParticipatingAgent.createParticipant(agent, participantCount++);
			this.connectedAgents.add(pa);
			
			// Initialize agent
			agent.initialize(pa.getParticipant());
			
		}
		lastToMove = null;
		
		broadcastMessage(new DialogueStartedMessage(topic, topicGoal), false);
		this.initialized = true;
		
	}
	
	public void run() {
		
		// Have we initialized yet?
		if (!initialized) {
			broadcastException(new PlatformException("The platform was started before it was initialized.", true));
			return;
		}
		this.paused = startPaused ;
		
		// As long as the dialogue is ongoing and we are not asked to pause, play a full game round
		while (dialogue.getState() != DialogueState.Terminated) {
			
			if (this.paused) {
				// We are going to pause
				broadcastMessage(new PlatformStateMessage(false), false);
			}
			synchronized (this) {
				while (this.paused) {
					try {
						wait();
						// We are going to run again if wait() succeeded (on notify())
						broadcastMessage(new PlatformStateMessage(true), false);
					} catch (Exception e) {
					}
				}
			}

			playDialogueRound();
			if (stopAfterEachStep) {
				this.paused = true;
			}
			
		}
		
	}
	
	/**
	 * Sets the platform start mode
	 * @param startPaused Whether to require an explicit requestStart() before deliberating
	 */
	public void setStartMode(boolean startPaused) {
		this.startPaused = startPaused;
	}

	/**
	 * Ask to start the platform execution (again).
	 */
	public void requestStart() {
		this.paused = false;
		notify();
	}

	/**
	 * Ask to pause the platform execution; normally the platform will 
	 * pause after the current round is completed. If you need to resume
	 * the process later on, use the .requestStart() method.
	 */
	public void requestPause() {
		this.paused = true;
	}

	/**
	 * Returns whether the platform is currently requested to be paused
	 * @return Returns true is the platform is (requested to be) paused; false otherwise
	 */
	public boolean isPaused() {
		return this.paused;
	}
	
	/**
	 * Set the single stepping behaviour of this platform
	 * @param stopAfterEachStep If set to true, the platform will stop running 
	 * after ever single dialogue round played; otherwise it will continue until 
	 * the dialogue terminates or requestStop() is called
	 */
	public void setSingleStepping(boolean stopAfterEachStep) {
		this.stopAfterEachStep = stopAfterEachStep;
	}
	
	/**
	 * Play a single dialogue round, depending on the current dialogue state
	 */
	private void playDialogueRound() {
		switch (dialogue.getState()) {
		case Unopened:
			// Unopened; only switch to the opening state
			setDialogueState(DialogueState.Joining);
			break;
		case Joining:
			// Joining; ask all non-opening agents to join the dialogue
			askAgentsToJoin();
			break;
		case Deliberating:
			// Deliberating; play a normal game round
			playDeliberationRound();
			break;
		case Terminating:
			// Terminating: determine the dialogue outcome
			determineOutcome();
			break;
		case Terminated:
			// Terminated; the dialogue will be stopped now
			break;
		}
	}
	
	private void setDialogueState(DialogueState newState) {
		broadcastMessage(new DialogueStateChangeMessage(newState), false);
		dialogue.setState(newState);
	}
	
	private ParticipatingAgent getNextToMove() {
		if (connectedAgents.size() == 0) {
			return null;
		}
		if (lastToMove == null) {
			// Nobody moved yet: return the first participant
			return connectedAgents.get(0);
		}
		for (int i = 0; i < connectedAgents.size(); i++) {
			if (connectedAgents.get(i).equals(lastToMove)) {
				if ((i + 1) < connectedAgents.size()) {
					// Return the next player in the row
					return connectedAgents.get(i + 1);
				} else {
					// The 'last' player moved last time; return to the first again
					return connectedAgents.get(0);
				}
			}
		}
		// Last player to move not found...
		return null;
	}
	
	private void askAgentsToJoin() {
		
		// Create an open-dialogue locution, mimicking the starting of the dialogue by one agent
		if (openDialogueMove == null) {
			openDialogueMove = Move.buildMove(null, null, new OpenDialogueLocution(dialogue.getTopic(), dialogue.getTopicGoal()));
			broadcastDialogeMove(openDialogueMove, true);
		}
		
		// Ask the next connected agent if they want to join the dialogue
		ParticipatingAgent toMove = getNextToMove();
		Move<? extends Locution> answer = toMove.getAgent().decideToJoin(openDialogueMove.getLocution());
		
		if (answer == null) {
			// The agent returned null; do not add it to the joining agents and send an exception
			toMove.getAgent().onDialogueException(new DialogueException("You should always return a join-dialogue or deny-dialogue move; instead null was returned. You did not join the dialogue."));
			broadcastException(new PlatformException(toMove.toString() + ", when asked to join the dialogue, returned null instead of a move.", false));
			
		} else if (answer.getLocution() instanceof DenyDialogueLocution) {
			// The agent denied to join; just let other agents and platform listeners know
			broadcastDialogeMove(answer, false);
			
		} else if (answer.getLocution() instanceof JoinDialogueLocution) {
			// The agent accepted; add it to the joined agents and let everybody know of it
			joinedAgents.add(toMove);
			broadcastDialogeMove(answer, false);
			
		} else {
			// If no valid answer was given, do not add it to the joining agents and send an exception
			toMove.getAgent().onDialogueException(new ProtocolException(answer, "You can only play a join-dialogue or deny-dialogue move as a reply to an open-dialogue. A deny is assumed and you will not join the dialogue."));
			broadcastException(new PlatformException(toMove.toString() + ", when asked to join the dialogue, played an invalid move " + answer.toLogicString(), false));
			
		}
		
		// Advance to the next participant
		lastToMove = toMove;
		if (connectedAgents.size() > 0 && connectedAgents.get(connectedAgents.size() - 1).equals(lastToMove)) {
			// If all agents have denied or joined the dialogue, advance to the next step
			setDialogueState(DialogueState.Deliberating);
		}
		
	}
	
	private void playDeliberationRound() {
		
		// Give the next agent to move a single turn to make new moves
		ParticipatingAgent toMove = getNextToMove();
		List<Move<? extends Locution>> moves = toMove.getAgent().makeMoves();

		// Check validity of the moves
		if (moves != null) {
			for (Iterator<Move<? extends Locution>> iter = moves.iterator(); iter.hasNext();) {
				Move<? extends Locution> check = iter.next();
				
				List<ProtocolException> reasons = checkMoveValidity(check);
				if (reasons != null && reasons.size() > 0) {
					
					// Some moves were invalid: send the reasons to the agent and notify any platform listeners
					for (ProtocolException reason : reasons) {
						toMove.getAgent().onDialogueException(reason);
					}
					broadcastException(new PlatformException(toMove.toString() + " made (some) invalid moves: " + reasons.toString(), false));
					
					// Since it was invalid, remove them from the actual played moves by this agent this turn
					iter.remove();
					
				}
			}
		}
		
		// If the agent made no (valid) moves: he skipped turn
		if (moves == null || moves.size() == 0) {
			broadcastMessage(new SkipMoveMessage(toMove.getParticipant()), true);
			skippedTurnsCount++;
			
		} else {
			
			skippedTurnsCount = 0;
			
			// Update the dialogue with the forwarded moves
			allMoves.addAll(moves);
			try {
				dialogue.update(moves);
			} catch (DialogueException e) {
				broadcastException(new PlatformException(e.getMessage(), false));
			}
			
			// Broadcast the moves made
			broadcastDialogeMoves(moves, true);

		}

		// Determine if the dialogue should end; if so, broadcast message and advance the dialogue to the terminating state
		TerminationMessage terminationCause = shouldTerminate();
		if (terminationCause != null) {
			
			// Broadcast entering of termination state of the dialogue
			broadcastMessage(terminationCause, true);
			setDialogueState(DialogueState.Terminating);
			
		} else {

			// Advance to the next participant
			lastToMove = toMove;
			
		}

		
	}
	
	private List<ProtocolException> checkMoveValidity(Move<? extends Locution> check) {
		// Ask all protocol rules if there is a reason why this move violates the protocol
		List<ProtocolException> reasons = new ArrayList<ProtocolException>();
		for (DeliberationRule rule : settings.getDeliberationRules()) {
			ProtocolException reason = rule.evaluateMove(dialogue, check);
			if (reason != null) {
				reasons.add(reason);
			}
		}
		return reasons; 
	}
	
	private TerminationMessage shouldTerminate() {
		// Look to the termination rules until there is one that says we should terminate
		for (TerminationRule rule : settings.getTerminationRules()) {
			TerminationMessage cause = rule.shouldTerminate(dialogue, joinedAgents.size(), skippedTurnsCount);
			if (cause != null) {
				return cause;
			}
		}
		return null;
	}

	private void determineOutcome() {
		
		// Get the dialogue outcome and broadcast this message
		broadcastMessage(new OutcomeMessage(settings.getOutcomeSelectionRule().determineOutcome(dialogue, allMoves)), false);
		
		// Set the dialogue state to terminated
		setDialogueState(DialogueState.Terminated);
		
	}

	/**
	 * Returns the list of agents that explicitly joined the dialogue
	 * TODO: Should this even be possible? Other way to access agent strategy stats?
	 * @return List of agent objects; should ONLY be used to access their statistics
	 */
	public List<ParticipatingAgent> getJoinedAgents() {
		return this.joinedAgents;
	}
	
	/**
	 * Attach an (error) message listener to this platform
	 * @param listener The message listener
	 */
	public void addListener(PlatformListener listener) {
		this.listeners.add(listener);
	}
	
	/**
	 * Broadcast newly made move by some agent to all connected or all joined agents (including itself) and to all platform listeners
	 * @param playedMove The new move that was played
	 * @param onlyJoinedAgents Whether the move should only be broadcasted to agents that accepted to join the dialogue; otherwise it it send to all agents connected to the platform 
	 */
	private void broadcastDialogeMove(Move<? extends Locution> playedMove, boolean onlyJoinedAgents) {
		// Note that Arrays.asList() doesn't work here because of the <? extends Locution>
		ArrayList<Move<? extends Locution>> list = new ArrayList<Move<? extends Locution>>();
		list.add(playedMove);
		broadcastDialogeMoves(list, onlyJoinedAgents);
	}
	
	/**
	 * Broadcast newly made moves by some agent to all connected or all joined agents (including itself) and to all platform listeners
	 * @param playedMoves The new moves that were played
	 * @param onlyJoinedAgents Whether the moves should only be broadcasted to agents that accepted to join the dialogue; otherwise it it send to all agents connected to the platform 
	 */
	private void broadcastDialogeMoves(List<Move<? extends Locution>> playedMoves, boolean onlyJoinedAgents) {
		
		// Broadcast to agents
		List<ParticipatingAgent> sendToAgents = (onlyJoinedAgents? joinedAgents: connectedAgents);
		for (ParticipatingAgent sendToAgent : sendToAgents) {
			sendToAgent.getAgent().onNewMovesReceived(playedMoves);
		}
		
		// Broadcast to platform listeners
		for (PlatformListener listener : listeners) {
			synchronized (listener) {
				listener.onMoves(playedMoves);
			}
		}
		
	}

	/**
	 * Broadcast a single dialogue message to all connected or all joined agents and to all platform listeners
	 * @param message The message about the dialogue (e.g. on a dialogue state change)
	 * @param onlyJoinedAgents Whether the message should only be broadcasted to agents that accepted to join the dialogue; otherwise it it send to all agents connected to the platform 
	 */
	private void broadcastMessage(DialogueMessage message, boolean onlyJoinedAgents) {
		broadcastMessages(Arrays.asList(message), onlyJoinedAgents);
	}
	
	/**
	 * Broadcast a set of dialogue messages to all connected or all joined agents and to all platform listeners
	 * @param messages The messages about the dialogue (e.g. on dialogue state changes)
	 * @param onlyJoinedAgents Whether the messages should only be broadcasted to agents that accepted to join the dialogue; otherwise it it send to all agents connected to the platform 
	 */
	private void broadcastMessages(List<DialogueMessage> messages, boolean onlyJoinedAgents) {
		
		// Broadcast to agents
		List<ParticipatingAgent> sendToAgents = (onlyJoinedAgents? joinedAgents: connectedAgents);
		for (ParticipatingAgent sendToAgent : sendToAgents) {
			sendToAgent.getAgent().onDialogueMessagesReceived(messages);
		}
		
		// Broadcast to platform listeners
		for (PlatformListener listener : listeners) {
			synchronized (listener) {
				listener.onMessagesReceived(messages);
			}
		}
		
	}
	
	/**
	 * Sends the (possibly critical) exception back to any listeners
	 * @param e The {@link PlatformException} that was thrown, which includes info on whether the exception was critical (i.e. if the platform needs to stop because of it)
	 * @return If any listeners were attached
	 */
	private boolean broadcastException(PlatformException e) {
		
		// If there are no listeners, return false
		if (listeners.size() == 0) {
			return false;
		}
		
		// Send the error message to each listener
		for (PlatformListener listener : listeners) {
			synchronized (listener) {
				listener.onExceptionThrown(e);
			}
		}
		return true;
		
	}
	
}
