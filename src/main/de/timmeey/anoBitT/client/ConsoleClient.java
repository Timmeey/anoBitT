package de.timmeey.anoBitT.client;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import asg.cliche.Command;
import asg.cliche.Param;

import com.google.gson.Gson;

import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.anoBitT.org.bitlet.wetorrent.TorrentManager;
import de.timmeey.anoBitT.peerGroup.PeerGroup;
import de.timmeey.anoBitT.peerGroup.PeerGroupApplicationOffer;
import de.timmeey.anoBitT.peerGroup.PeerGroupManager;
import de.timmeey.anoBitT.peerGroup.Member.PeerGroupMember;

public class ConsoleClient {
	private final PeerGroupManager peerGroupManager;
	private final DHTService dhtService;
	private final TorrentManager torrentManager;

	public ConsoleClient(PeerGroupManager peerGroupManager,
			DHTService dhtService, TorrentManager torrentManager) {
		super();
		this.peerGroupManager = checkNotNull(peerGroupManager);
		this.dhtService = checkNotNull(dhtService);
		this.torrentManager = checkNotNull(torrentManager);
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
		List<String> result = dhtService.get(key);
		System.out.println(Arrays.toString(result.toArray()));
		return result.get(0);
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
		System.out.println(String.format("MembersIP before: %s",
				member.getIpAddress()));

		member.updateIpAddress();
		System.out.println(String.format("MembersIP after: %s",
				member.getIpAddress()));

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

	@Command(description = "Creates a new application offer")
	public void createApplicationOffer(String uuid) {
		Optional<PeerGroup> group = peerGroupManager.getPeerGroupByUUID(UUID
				.fromString(uuid));
		if (group.isPresent()) {
			PeerGroupApplicationOffer offer = group.get()
					.createApplicationOffer();
			System.out.println(String.format("Key is: %s",
					offer.getSecretOneTimePassword()));
		} else {
			System.out.println("Failed to create offer, could not find group");
		}
	}

	@Command
	public void tryToJoinWithOffer(String secretOneTimePassword) {
		Optional<PeerGroup> joinedGroup = peerGroupManager
				.tryToJoinWithOffer(secretOneTimePassword);
		if (joinedGroup.isPresent()) {
			System.out.println(String.format(
					"Seems like it worked, got group %s", joinedGroup.get()
							.getUUID()));
		} else {
			System.out.println("That doesn't look good");
		}

	}

	@Command
	public void addTorrent(String torrentFile, String nameToStore)
			throws Exception {
		torrentManager.startTorrent(torrentFile, nameToStore);
	}

	@Command
	public void init() {
		PeerGroup group = createPeerGroup("first");
		createApplicationOffer(group.getUUID().toString());
	}

}
