import type { SocialUser } from "../types";

export const socialUsers: SocialUser[] = [
  {
    id: "user-kavya",
    name: "Kavya Nair",
    username: "kavya",
    initials: "KN",
    gradient: "from-violet-400 to-cyan-300",
    bio: "Building knowledge graphs that teams actually use. Ontology nerd.",
    status: "online",
    stats: { posts: 142, followers: 1284, following: 312 }
  },
  {
    id: "user-maya",
    name: "Maya Chen",
    username: "maya",
    initials: "MC",
    gradient: "from-fuchsia-400 to-violet-500",
    bio: "Product intelligence lead. Connecting signals to strategy.",
    status: "online",
    stats: { posts: 89, followers: 2103, following: 428 }
  },
  {
    id: "user-ari",
    name: "Ari Patel",
    username: "ari",
    initials: "AP",
    gradient: "from-emerald-400 to-cyan-400",
    bio: "Graph architect. Published Ontology v18 last week.",
    status: "away",
    stats: { posts: 67, followers: 892, following: 201 }
  },
  {
    id: "user-ava",
    name: "Ava Rodriguez",
    username: "ava",
    initials: "AR",
    gradient: "from-amber-300 to-orange-400",
    bio: "Research ops. Cleaning duplicate concepts since 2024.",
    status: "offline",
    stats: { posts: 54, followers: 645, following: 178 }
  },
  {
    id: "user-leo",
    name: "Leo Kim",
    username: "leo",
    initials: "LK",
    gradient: "from-sky-400 to-indigo-500",
    bio: "ML engineer. Embedding search & cluster discovery.",
    status: "online",
    stats: { posts: 31, followers: 412, following: 156 }
  },
  {
    id: "user-sam",
    name: "Sam Ortiz",
    username: "sam",
    initials: "SO",
    gradient: "from-rose-400 to-pink-500",
    bio: "Customer signals analyst. Voice-of-customer graphs.",
    status: "away",
    stats: { posts: 44, followers: 523, following: 189 }
  }
];

export function getUserByUsername(username: string) {
  return socialUsers.find((user) => user.username === username);
}
