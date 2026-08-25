import type { SocialComment, SocialPost } from "../types";

const baseComments: SocialComment[] = [
  {
    id: "c1",
    authorId: "user-maya",
    content: "This cluster mapping is exactly what we needed for Q3 planning. @kavya did you pull from the Customer Signals graph?",
    createdAt: "2026-07-28T14:22:00Z",
    parentId: null,
    reactions: [
      { emoji: "👍", userIds: ["user-ari", "user-leo"] },
      { emoji: "💡", userIds: ["user-kavya"] }
    ],
    likes: 8,
    likedByCurrentUser: false
  },
  {
    id: "c2",
    authorId: "user-kavya",
    content: "Yes! Merged signals from Customer Signals + Research Memory. @maya want to review the edge weights together?",
    createdAt: "2026-07-28T14:35:00Z",
    parentId: "c1",
    reactions: [{ emoji: "🚀", userIds: ["user-maya"] }],
    likes: 3,
    likedByCurrentUser: true
  },
  {
    id: "c3",
    authorId: "user-ari",
    content: "The ontology alignment here is clean. I'd suggest tagging the decision nodes with confidence scores.",
    createdAt: "2026-07-28T14:48:00Z",
    parentId: "c1",
    reactions: [{ emoji: "🔥", userIds: ["user-kavya", "user-maya"] }],
    likes: 5,
    likedByCurrentUser: false
  },
  {
    id: "c4",
    authorId: "user-leo",
    content: "Embedding similarity picked up 3 duplicate concepts automatically. Should I auto-merge?",
    createdAt: "2026-07-28T15:02:00Z",
    parentId: null,
    reactions: [],
    likes: 2,
    likedByCurrentUser: false
  },
  {
    id: "c5",
    authorId: "user-ava",
    content: "Hold off on auto-merge — I flagged two of those as intentional variants in the last cleanup sprint.",
    createdAt: "2026-07-28T15:18:00Z",
    parentId: "c4",
    reactions: [{ emoji: "👍", userIds: ["user-leo", "user-kavya"] }],
    likes: 4,
    likedByCurrentUser: true
  }
];

function cloneComments(): SocialComment[] {
  return baseComments.map((comment) => ({
    ...comment,
    reactions: comment.reactions.map((reaction) => ({
      ...reaction,
      userIds: [...reaction.userIds]
    }))
  }));
}

const postTemplates: Omit<SocialPost, "id">[] = [
  {
    workspaceId: "default-workspace",
    authorId: "user-maya",
    content:
      "Just linked the Roadmap cluster to Customer Signals — 47 new edges discovered. @kavya @ari this changes our Q3 priority map significantly. 🚀",
    entityType: "Graph",
    entityName: "Product Intelligence",
    createdAt: "2026-07-28T13:45:00Z",
    likes: 24,
    likedByCurrentUser: false,
    reactions: [
      { emoji: "🚀", userIds: ["user-kavya", "user-leo", "user-sam"] },
      { emoji: "🔥", userIds: ["user-ari"] },
      { emoji: "💡", userIds: ["user-ava"] }
    ],
    comments: cloneComments(),
    favorites: 5,
    favoritedByCurrentUser: false,
    commentCount: 5
  },
  {
    workspaceId: "default-workspace",
    authorId: "user-ari",
    content: "Published Ontology v18 with 12 new relationship types. Migration guide is in the Decision Archive graph. Feedback welcome!",
    entityType: "Document",
    entityName: "Ontology v18",
    createdAt: "2026-07-28T11:20:00Z",
    likes: 18,
    likedByCurrentUser: true,
    reactions: [
      { emoji: "🎉", userIds: ["user-maya", "user-kavya", "user-leo", "user-ava"] },
      { emoji: "👍", userIds: ["user-sam"] }
    ],
    comments: [
      {
        id: "c6",
        authorId: "user-kavya",
        authorName: "Kavya Nair",
        authorUsername: "kavya",
        content: "Incredible work @ari! The temporal edge type is a game-changer for our roadmap views.",
        createdAt: "2026-07-28T11:45:00Z",
        parentId: null,
        reactions: [{ emoji: "❤️", userIds: ["user-ari"] }],
        likes: 6,
        likedByCurrentUser: false
      }
    ],
    favorites: 3,
    favoritedByCurrentUser: false,
    commentCount: 1
  },
  {
    workspaceId: "default-workspace",
    authorId: "user-leo",
    content:
      "AI suggested 12 duplicate concepts in Research Memory. Running embedding dedup now — @ava can you sanity-check the merge candidates?",
    entityType: "Cluster",
    entityName: "Research Memory",
    createdAt: "2026-07-28T09:30:00Z",
    likes: 11,
    likedByCurrentUser: false,
    reactions: [{ emoji: "💡", userIds: ["user-ava", "user-maya"] }],
    comments: [],
    favorites: 1,
    favoritedByCurrentUser: false,
    commentCount: 0
  },
  {
    workspaceId: "default-workspace",
    authorId: "user-ava",
    content: "Completed ontology cleanup sprint — removed 340 orphan nodes and re-indexed 2.1k relationships. Graph health back to 99.3%.",
    entityType: "Graph",
    entityName: "Customer Signals",
    createdAt: "2026-07-27T16:00:00Z",
    likes: 32,
    likedByCurrentUser: false,
    reactions: [
      { emoji: "🔥", userIds: ["user-kavya", "user-maya", "user-ari", "user-leo"] },
      { emoji: "👍", userIds: ["user-sam"] }
    ],
    comments: [
      {
        id: "c7",
        authorId: "user-sam",
        authorName: "Sam Ortiz",
        authorUsername: "sam",
        content: "The voice-of-customer subgraph looks much cleaner now. @ava thank you!",
        createdAt: "2026-07-27T16:30:00Z",
        parentId: null,
        reactions: [],
        likes: 2,
        likedByCurrentUser: false
      },
      {
        id: "c8",
        authorId: "user-maya",
        authorName: "Maya Chen",
        authorUsername: "maya",
        content: "Can we schedule a walkthrough for the product team?",
        createdAt: "2026-07-27T17:00:00Z",
        parentId: "c7",
        reactions: [{ emoji: "👍", userIds: ["user-ava"] }],
        likes: 1,
        likedByCurrentUser: false
      }
    ],
    favorites: 8,
    favoritedByCurrentUser: false,
    commentCount: 2
  },
  {
    workspaceId: "default-workspace",
    authorId: "user-sam",
    content: "New customer interview insights mapped to 8 decision nodes. @maya the pricing sensitivity cluster grew 40% this week.",
    entityType: "Cluster",
    entityName: "Market Map",
    createdAt: "2026-07-27T14:15:00Z",
    likes: 15,
    likedByCurrentUser: true,
    reactions: [{ emoji: "💡", userIds: ["user-maya", "user-kavya"] }],
    comments: [],
    favorites: 2,
    favoritedByCurrentUser: false,
    commentCount: 0
  },
  {
    workspaceId: "default-workspace",
    authorId: "user-kavya",
    content: "Created Market Map cluster with competitive positioning edges. @leo can you run similarity against the Product Intelligence graph?",
    entityType: "Graph",
    entityName: "Market Map",
    createdAt: "2026-07-27T10:00:00Z",
    likes: 9,
    likedByCurrentUser: false,
    reactions: [{ emoji: "🚀", userIds: ["user-leo"] }],
    comments: [],
    favorites: 0,
    favoritedByCurrentUser: false,
    commentCount: 0
  }
];

function generatePosts(count: number): SocialPost[] {
  const posts: SocialPost[] = [];
  for (let index = 0; index < count; index++) {
    const template = postTemplates[index % postTemplates.length];
    if (!template) continue;

    const dayOffset = Math.floor(index / postTemplates.length);
    const baseDate = new Date(template.createdAt);
    baseDate.setDate(baseDate.getDate() - dayOffset);

    posts.push({
      ...template,
      id: `post-${index + 1}`,
      createdAt: baseDate.toISOString(),
      comments: template.comments.map((comment) => ({
        ...comment,
        id: `${comment.id}-p${index}`,
        reactions: comment.reactions.map((reaction) => ({
          ...reaction,
          userIds: [...reaction.userIds]
        }))
      })),
      reactions: template.reactions.map((reaction) => ({
        ...reaction,
        userIds: [...reaction.userIds]
      }))
    });
  }
  return posts;
}

export const allPosts = generatePosts(24);

const PAGE_SIZE = 5;

export function fetchFeedPage(cursor: number): { posts: SocialPost[]; hasMore: boolean; nextCursor: number } {
  const posts = allPosts.slice(cursor, cursor + PAGE_SIZE);
  const nextCursor = cursor + PAGE_SIZE;
  return {
    posts,
    hasMore: nextCursor < allPosts.length,
    nextCursor
  };
}

export function simulateNetworkDelay(ms = 600) {
  return new Promise<void>((resolve) => setTimeout(resolve, ms));
}
