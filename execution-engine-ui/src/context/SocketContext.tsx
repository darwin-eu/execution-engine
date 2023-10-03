import { Client } from "@stomp/stompjs";
import React, { PropsWithChildren, createContext, useState } from "react";

import { Analysis } from "../@types/data-source";

export type SocketContextProps = { updatedAnalysis: Analysis | undefined };

const base = window._env_.BACKEND_BASE_URL;

export const SocketContext = createContext<SocketContextProps | null>(null);

const SocketProvider: React.FC<PropsWithChildren> = ({ children }) => {
  const [connected, setConnected] = useState(false);
  const [updatedAnalysis, setUpdatedAnalysis] = useState<Analysis | undefined>(
    undefined,
  );

  let socketBase: string;
  if (base.startsWith("https")) {
    socketBase = base.replace("https", "wss");
  } else {
    socketBase = base.replace("http", "ws");
  }

  if (!connected) {
    const websocket = new Client({
      brokerURL: `${socketBase}/execution-engine-ws`,
    });

    websocket.onConnect = () => {
      websocket.subscribe("/topic/submissions", (message) => {
        const a = JSON.parse(message.body) as Analysis;
        setUpdatedAnalysis(a);
      });
      setConnected(true);
    };

    websocket.onStompError = (frame) => {
      console.error("Broker reported error: " + frame.headers["message"]);
      console.error("Additional details: " + frame.body);
    };

    websocket.activate();
  }

  return (
    <SocketContext.Provider value={{ updatedAnalysis }}>
      {children}
    </SocketContext.Provider>
  );
};

export default SocketProvider;
