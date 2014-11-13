package de.timmeey.anoBitT.peerGroup;

import java.util.List;
import java.util.concurrent.Future;

import de.timmeey.anoBitT.peerGroup.Member.Member;

public abstract class PeerGroup {

	/**
	 * Get the unique name of the group
	 * 
	 * @return the unique group name
	 */
	public abstract String getName();

	/**
	 * Gets the humanReadable Name of the group
	 * 
	 * @return the human readable name of the group
	 */
	public abstract String getHumanReadableGroupName();

	/**
	 * Returns all the current member
	 * 
	 * @return current member
	 */
	public abstract List<Member> getMembers();

	/**
	 * Returns the time of the last List update
	 * 
	 * @return the update time
	 */
	public abstract long getLastUpdateTime();

	/**
	 * Gets a specific group member by his onion address, or null if not found
	 * 
	 * @param onionAddress
	 *            the onion address of the wanted user
	 * @return the specified user or null
	 */
	public abstract Member getMember(String onionAddress);

	/**
	 * Adds a Member to a PeerGroup. Used for initial addition of a member. NOT
	 * used for updates of the group
	 * 
	 * @param member
	 *            The new member
	 * @return the peerGroup with the added member
	 */
	public abstract PeerGroup addMember(Member member);

	/**
	 * Updates the PeerGroup by querying other members for updated information
	 * Intended to be called while starting the application
	 * 
	 * @return A Future with the updated PeerGroup
	 */
	public abstract Future<PeerGroup> updateGroupMembers();

	/**
	 * Updates the group by querying the specified Member for new Information
	 * Intended to be called when a Peer indicates he has new information
	 * 
	 * @param member
	 *            the member indication it has new Information
	 * @return a future with the updated peerGroup
	 */
	public abstract Future<PeerGroup> updateGroupMembers(Member member);

	/**
	 * Updates the online status of all group Members bz asking them for their
	 * IP Intended to be called on startup or periodically
	 * 
	 * @return A future with the updated peerGroup
	 */
	public abstract Future<PeerGroup> updateOnlineStats();

}
