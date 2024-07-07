package capsule.network;

import capsule.CapsuleMod;
import capsule.network.handler.ClientPayloadHandler;
import capsule.network.handler.ServerPayloadHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

public class CapsuleNetwork {
    public static void setupPackets(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar(CapsuleMod.MODID);

        // client ask server to edit capsule label
        registrar.play(LabelEditedMessageToServer.ID, LabelEditedMessageToServer::new, handler -> handler
                .server(ServerPayloadHandler.getInstance()::handleLabel));
        // client ask server data needed to preview a deploy
        registrar.play(CapsuleContentPreviewQueryToServer.ID, CapsuleContentPreviewQueryToServer::new, handler -> handler
                .server(ServerPayloadHandler.getInstance()::handleContentPreviewQuery));
        // client ask server to throw item to a specific position
        registrar.play(CapsuleThrowQueryToServer.ID, CapsuleThrowQueryToServer::new, handler -> handler
                .server(ServerPayloadHandler.getInstance()::handleThrowQuery));
        // client ask server to reload the held blueprint capsule
        registrar.play(CapsuleLeftClickQueryToServer.ID, CapsuleLeftClickQueryToServer::new, handler -> handler
                .server(ServerPayloadHandler.getInstance()::handleLeftClickQuery));

        // server sends to client the data needed to preview a deploy
        registrar.play(CapsuleContentPreviewAnswerToClient.ID, CapsuleContentPreviewAnswerToClient::new, handler -> handler
                .client(ClientPayloadHandler.getInstance()::handleContentPreviewAnswer));
        // server sends to client the data needed to render undeploy
        registrar.play(CapsuleUndeployNotifToClient.ID, CapsuleUndeployNotifToClient::new, handler -> handler
                .client(ClientPayloadHandler.getInstance()::handleUndeployNotif));
        // server sends to client the full NBT for display
        registrar.play(CapsuleFullContentAnswerToClient.ID, CapsuleFullContentAnswerToClient::new, handler -> handler
                .client(ClientPayloadHandler.getInstance()::handleFullContentAnswer));

    }
}
