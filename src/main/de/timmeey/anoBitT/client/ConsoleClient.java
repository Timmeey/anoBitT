package de.timmeey.anoBitT.client;

import java.util.UUID;

import com.google.gson.Gson;

import asg.cliche.Command;
import asg.cliche.Param;
import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.anoBitT.exceptions.NotOnlineException;
import de.timmeey.anoBitT.peerGroup.PeerGroup;
import de.timmeey.anoBitT.peerGroup.PeerGroupManager;
import de.timmeey.anoBitT.peerGroup.Member.PeerGroupMember;

public class ConsoleClient {
	private final PeerGroupManager peerGroupManager;
	private final DHTService dhtService;

	public ConsoleClient(PeerGroupManager peerGroupManager,
			DHTService dhtService) {
		super();
		this.peerGroupManager = peerGroupManager;
		this.dhtService = dhtService;
	}

	@Command(description = "Puts a value into the DHT", abbrev = "put")
	public boolean putValueToDHT(
			@Param(name = "key", description = "Key for the value") String key,
			@Param(name = "value", description = "The value") String value,
			@Param(name = "wait", description = "Boolean whether to wait for confirmation or not") boolean wait) {
		boolean result = dhtService.put(key, value, wait);
		System.out.println(result);
		return result;
	}

	@Command(description = "Gets a value from the DHT", abbrev = "get")
	public String getValueFromDHT(
			@Param(name = "key", description = "Key for the value") String key) {
		String value = dhtService.get(key);
		return value;
	}

	@Command(description = "Creates a new PeerGroup", abbrev = "createPeerGroup")
	public PeerGroup createPeerGroup(
			@Param(name = "key", description = "Key for the value") String groupName) {
		PeerGroup createdGroup = peerGroupManager.createPeerGroup(groupName);
		return createdGroup;
	}

	@Command(description = "Requests an updated IP address from a member")
	public PeerGroupMember requestUpdateIP(
			@Param(name = "uuid", description = "The uuid of the PeerGroup the member is in") String uuid,
			@Param(name = "onionAddress", description = "The onionAddress of the Member that should updateItsIp") String onionAddress) {
		UUID uuid1 = UUID.fromString(uuid);
		PeerGroupMember member = peerGroupManager.getPeerGroupByUUID(uuid1)
				.get().getMember(onionAddress).get();
		try {
			System.out.println(String.format("MembersIP before: %s",
					member.getIpAddress()));
		} catch (NotOnlineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		member.updateIpAddress();
		try {
			System.out.println(String.format("MembersIP after: %s",
					member.getIpAddress()));
		} catch (NotOnlineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return member;
	}

	@Command(description = "Requests an update of the memberlist of a PeerGroup")
	public PeerGroup requestMemberUpdate(
			@Param(name = "uuid", description = "The uuid of the PeerGroup") String uuid) {
		UUID uuid1 = UUID.fromString(uuid);
		PeerGroup group = peerGroupManager.getPeerGroupByUUID(uuid1).get();
		Gson gson = new Gson();
		System.out.println(gson.toJson(group));
		System.out.println(group);
		group.updateGroupMembers();

		return group;
	}

}
